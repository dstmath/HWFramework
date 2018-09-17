package com.android.server.accessibility;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.IAccessibilityServiceClient;
import android.accessibilityservice.IAccessibilityServiceConnection;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Region.Op;
import android.hardware.display.DisplayManager;
import android.hardware.fingerprint.IFingerprintService;
import android.hardware.input.InputManager;
import android.hdm.HwDeviceManager;
import android.media.AudioManagerInternal;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.UserManagerInternal;
import android.provider.Settings.Secure;
import android.provider.SettingsStringUtil.ComponentNameSet;
import android.provider.SettingsStringUtil.SettingStringHelper;
import android.text.TextUtils;
import android.text.TextUtils.SimpleStringSplitter;
import android.util.IntArray;
import android.util.Slog;
import android.util.SparseArray;
import android.view.Display;
import android.view.IWindow;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MagnificationSpec;
import android.view.View;
import android.view.WindowInfo;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerInternal;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityInteractionClient;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.view.accessibility.AccessibilityWindowInfo;
import android.view.accessibility.IAccessibilityInteractionConnection;
import android.view.accessibility.IAccessibilityInteractionConnectionCallback;
import android.view.accessibility.IAccessibilityManager.Stub;
import android.view.accessibility.IAccessibilityManagerClient;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.content.PackageMonitor;
import com.android.internal.os.SomeArgs;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.IntPair;
import com.android.server.LocalServices;
import com.android.server.accessibility.FingerprintGestureDispatcher.FingerprintGestureClient;
import com.android.server.accessibility.KeyEventDispatcher.KeyEventFilter;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.lights.LightsManager;
import com.android.server.os.HwBootFail;
import com.android.server.policy.AccessibilityShortcutController;
import com.android.server.power.IHwShutdownThread;
import com.android.server.statusbar.StatusBarManagerInternal;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class AccessibilityManagerService extends Stub {
    private static final char COMPONENT_NAME_SEPARATOR = ':';
    private static final boolean DEBUG = false;
    private static final String FUNCTION_DUMP = "dump";
    private static final String FUNCTION_REGISTER_UI_TEST_AUTOMATION_SERVICE = "registerUiTestAutomationService";
    private static final String GET_WINDOW_TOKEN = "getWindowToken";
    private static final String LOG_TAG = "AccessibilityManagerService";
    public static final int MAGNIFICATION_GESTURE_HANDLER_ID = 0;
    private static final int OWN_PROCESS_ID = Process.myPid();
    private static final String SET_PIP_ACTION_REPLACEMENT = "setPictureInPictureActionReplacingConnection";
    private static final String TEMPORARY_ENABLE_ACCESSIBILITY_UNTIL_KEYGUARD_REMOVED = "temporaryEnableAccessibilityStateUntilKeyguardRemoved";
    private static final int WAIT_FOR_USER_STATE_FULLY_INITIALIZED_MILLIS = 3000;
    private static final int WAIT_MOTION_INJECTOR_TIMEOUT_MILLIS = 1000;
    private static final int WAIT_WINDOWS_TIMEOUT_MILLIS = 5000;
    private static final ComponentName sFakeAccessibilityServiceComponentName = new ComponentName("foo.bar", "FakeService");
    private static int sIdCounter = 1;
    private static int sNextWindowId;
    private final Context mContext;
    private int mCurrentUserId = 0;
    private AlertDialog mEnableTouchExplorationDialog;
    private FingerprintGestureDispatcher mFingerprintGestureDispatcher;
    private final RemoteCallbackList<IAccessibilityManagerClient> mGlobalClients = new RemoteCallbackList();
    private final SparseArray<AccessibilityConnectionWrapper> mGlobalInteractionConnections = new SparseArray();
    private final SparseArray<IBinder> mGlobalWindowTokens = new SparseArray();
    private boolean mHasInputFilter;
    private boolean mInitialized;
    private AccessibilityInputFilter mInputFilter;
    private InteractionBridge mInteractionBridge;
    private boolean mIsAccessibilityButtonShown;
    private KeyEventDispatcher mKeyEventDispatcher;
    private final Object mLock = new Object();
    private MagnificationController mMagnificationController;
    private final MainHandler mMainHandler;
    private MotionEventInjector mMotionEventInjector;
    private final PackageManager mPackageManager;
    private AccessibilityConnectionWrapper mPictureInPictureActionReplacingConnection;
    private final PowerManager mPowerManager;
    private final SecurityPolicy mSecurityPolicy;
    private final SimpleStringSplitter mStringColonSplitter = new SimpleStringSplitter(COMPONENT_NAME_SEPARATOR);
    private final List<AccessibilityServiceInfo> mTempAccessibilityServiceInfoList = new ArrayList();
    private final Set<ComponentName> mTempComponentNameSet = new HashSet();
    private final IntArray mTempIntArray = new IntArray(0);
    private final Point mTempPoint = new Point();
    private final Rect mTempRect = new Rect();
    private final Rect mTempRect1 = new Rect();
    private final UserManager mUserManager;
    private final SparseArray<UserState> mUserStates = new SparseArray();
    private final WindowManagerInternal mWindowManagerService;
    private WindowsForAccessibilityCallback mWindowsForAccessibilityCallback;

    private class AccessibilityConnectionWrapper implements DeathRecipient {
        private final IAccessibilityInteractionConnection mConnection;
        private final int mUserId;
        private final int mWindowId;

        public AccessibilityConnectionWrapper(int windowId, IAccessibilityInteractionConnection connection, int userId) {
            this.mWindowId = windowId;
            this.mUserId = userId;
            this.mConnection = connection;
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

    private final class AccessibilityContentObserver extends ContentObserver {
        private final Uri mAccessibilityButtonComponentIdUri = Secure.getUriFor("accessibility_button_target_component");
        private final Uri mAccessibilityShortcutServiceIdUri = Secure.getUriFor("accessibility_shortcut_target_service");
        private final Uri mAccessibilitySoftKeyboardModeUri = Secure.getUriFor("accessibility_soft_keyboard_mode");
        private final Uri mAutoclickEnabledUri = Secure.getUriFor("accessibility_autoclick_enabled");
        private final Uri mDisplayDaltonizerEnabledUri = Secure.getUriFor("accessibility_display_daltonizer_enabled");
        private final Uri mDisplayDaltonizerUri = Secure.getUriFor("accessibility_display_daltonizer");
        private final Uri mDisplayInversionEnabledUri = Secure.getUriFor("accessibility_display_inversion_enabled");
        private final Uri mDisplayMagnificationEnabledUri = Secure.getUriFor("accessibility_display_magnification_enabled");
        private final Uri mEnabledAccessibilityServicesUri = Secure.getUriFor("enabled_accessibility_services");
        private final Uri mHighTextContrastUri = Secure.getUriFor("high_text_contrast_enabled");
        private final Uri mNavBarMagnificationEnabledUri = Secure.getUriFor("accessibility_display_magnification_navbar_enabled");
        private final Uri mTouchExplorationEnabledUri = Secure.getUriFor("touch_exploration_enabled");
        private final Uri mTouchExplorationGrantedAccessibilityServicesUri = Secure.getUriFor("touch_exploration_granted_accessibility_services");

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

        /* JADX WARNING: Missing block: B:14:0x002b, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onChange(boolean selfChange, Uri uri) {
            synchronized (AccessibilityManagerService.this.mLock) {
                UserState userState = AccessibilityManagerService.this.getCurrentUserStateLocked();
                if (userState.isUiAutomationSuppressingOtherServices()) {
                } else if (this.mTouchExplorationEnabledUri.equals(uri)) {
                    if (AccessibilityManagerService.this.readTouchExplorationEnabledSettingLocked(userState)) {
                        AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                    }
                } else if (this.mDisplayMagnificationEnabledUri.equals(uri) || this.mNavBarMagnificationEnabledUri.equals(uri)) {
                    if (AccessibilityManagerService.this.readMagnificationEnabledSettingsLocked(userState)) {
                        AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                    }
                } else if (this.mAutoclickEnabledUri.equals(uri)) {
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
                } else if (this.mDisplayDaltonizerEnabledUri.equals(uri) || this.mDisplayDaltonizerUri.equals(uri)) {
                    AccessibilityManagerService.this.updateDisplayDaltonizerLocked(userState);
                } else if (this.mDisplayInversionEnabledUri.equals(uri)) {
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
    }

    private final class InteractionBridge {
        private final AccessibilityInteractionClient mClient = AccessibilityInteractionClient.getInstance();
        private final int mConnectionId;
        private final Display mDefaultDisplay;

        public InteractionBridge() {
            AccessibilityServiceInfo info = new AccessibilityServiceInfo();
            info.setCapabilities(1);
            info.flags |= 64;
            info.flags |= 2;
            Service service = new Service(-10000, AccessibilityManagerService.sFakeAccessibilityServiceComponentName, info);
            this.mConnectionId = service.mId;
            this.mClient.addConnection(this.mConnectionId, service);
            this.mDefaultDisplay = ((DisplayManager) AccessibilityManagerService.this.mContext.getSystemService("display")).getDisplay(0);
        }

        public void clearAccessibilityFocusNotLocked(int windowId) {
            AccessibilityNodeInfo focus = getAccessibilityFocusNotLocked(windowId);
            if (focus != null) {
                focus.performAction(128);
            }
        }

        public boolean performActionOnAccessibilityFocusedItemNotLocked(AccessibilityAction action) {
            AccessibilityNodeInfo focus = getAccessibilityFocusNotLocked();
            if (focus == null || (focus.getActionList().contains(action) ^ 1) != 0) {
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
                if (!(spec == null || (spec.isNop() ^ 1) == 0)) {
                    boundsInScreen.offset((int) (-spec.offsetX), (int) (-spec.offsetY));
                    boundsInScreen.scale(1.0f / spec.scale);
                }
                Rect windowBounds = AccessibilityManagerService.this.mTempRect1;
                AccessibilityManagerService.this.getWindowBounds(focus.getWindowId(), windowBounds);
                if (boundsInScreen.intersect(windowBounds)) {
                    Point screenSize = AccessibilityManagerService.this.mTempPoint;
                    this.mDefaultDisplay.getRealSize(screenSize);
                    if (boundsInScreen.intersect(0, 0, screenSize.x, screenSize.y)) {
                        outPoint.set(boundsInScreen.centerX(), boundsInScreen.centerY());
                        return true;
                    }
                    return false;
                }
                return false;
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

    private final class MainHandler extends Handler {
        public static final int MSG_ANNOUNCE_NEW_USER_IF_NEEDED = 5;
        public static final int MSG_CLEAR_ACCESSIBILITY_FOCUS = 9;
        public static final int MSG_SEND_ACCESSIBILITY_BUTTON_TO_INPUT_FILTER = 13;
        public static final int MSG_SEND_ACCESSIBILITY_EVENT_TO_INPUT_FILTER = 1;
        public static final int MSG_SEND_CLEARED_STATE_TO_CLIENTS_FOR_USER = 3;
        public static final int MSG_SEND_KEY_EVENT_TO_INPUT_FILTER = 8;
        public static final int MSG_SEND_RELEVANT_EVENTS_CHANGED_TO_CLIENTS = 12;
        public static final int MSG_SEND_SERVICES_STATE_CHANGED_TO_CLIENTS = 10;
        public static final int MSG_SEND_STATE_TO_CLIENTS = 2;
        public static final int MSG_SHOW_ACCESSIBILITY_BUTTON_CHOOSER = 14;
        public static final int MSG_SHOW_ENABLED_TOUCH_EXPLORATION_DIALOG = 7;
        public static final int MSG_UPDATE_FINGERPRINT = 11;
        public static final int MSG_UPDATE_INPUT_FILTER = 6;

        public MainHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            int userId;
            switch (msg.what) {
                case 1:
                    AccessibilityEvent event = msg.obj;
                    synchronized (AccessibilityManagerService.this.mLock) {
                        if (AccessibilityManagerService.this.mHasInputFilter && AccessibilityManagerService.this.mInputFilter != null) {
                            AccessibilityManagerService.this.mInputFilter.notifyAccessibilityEvent(event);
                        }
                    }
                    event.recycle();
                    return;
                case 2:
                    int clientState = msg.arg1;
                    userId = msg.arg2;
                    sendStateToClients(clientState, AccessibilityManagerService.this.mGlobalClients);
                    sendStateToClients(clientState, getUserClientsForId(userId));
                    return;
                case 3:
                    sendStateToClients(0, getUserClientsForId(msg.arg1));
                    return;
                case 5:
                    announceNewUserIfNeeded();
                    return;
                case 6:
                    AccessibilityManagerService.this.updateInputFilter(msg.obj);
                    return;
                case 7:
                    AccessibilityManagerService.this.showEnableTouchExplorationDialog(msg.obj);
                    return;
                case 8:
                    KeyEvent event2 = msg.obj;
                    int policyFlags = msg.arg1;
                    synchronized (AccessibilityManagerService.this.mLock) {
                        if (AccessibilityManagerService.this.mHasInputFilter && AccessibilityManagerService.this.mInputFilter != null) {
                            AccessibilityManagerService.this.mInputFilter.sendInputEvent(event2, policyFlags);
                        }
                    }
                    event2.recycle();
                    return;
                case 9:
                    AccessibilityManagerService.this.getInteractionBridge().clearAccessibilityFocusNotLocked(msg.arg1);
                    return;
                case 10:
                    userId = msg.arg1;
                    notifyClientsOfServicesStateChange(AccessibilityManagerService.this.mGlobalClients);
                    notifyClientsOfServicesStateChange(getUserClientsForId(userId));
                    return;
                case 11:
                    AccessibilityManagerService.this.updateFingerprintGestureHandling((UserState) msg.obj);
                    return;
                case 12:
                    UserState userState;
                    userId = msg.arg1;
                    int relevantEventTypes = msg.arg2;
                    synchronized (AccessibilityManagerService.this.mLock) {
                        userState = AccessibilityManagerService.this.getUserStateLocked(userId);
                    }
                    AccessibilityManagerService.this.broadcastToClients(userState, new -$Lambda$kXhldx_ZxidxR4suyNIbZ545MMw(relevantEventTypes));
                    return;
                case 13:
                    synchronized (AccessibilityManagerService.this.mLock) {
                        if (AccessibilityManagerService.this.mHasInputFilter && AccessibilityManagerService.this.mInputFilter != null) {
                            AccessibilityManagerService.this.mInputFilter.notifyAccessibilityButtonClicked();
                        }
                    }
                    return;
                case 14:
                    AccessibilityManagerService.this.showAccessibilityButtonTargetSelection();
                    return;
                default:
                    return;
            }
        }

        static /* synthetic */ void lambda$-com_android_server_accessibility_AccessibilityManagerService$MainHandler_113743(int relevantEventTypes, IAccessibilityManagerClient client) {
            try {
                client.setRelevantEventTypes(relevantEventTypes);
            } catch (RemoteException e) {
            }
        }

        private void announceNewUserIfNeeded() {
            synchronized (AccessibilityManagerService.this.mLock) {
                if (AccessibilityManagerService.this.getCurrentUserStateLocked().isHandlingAccessibilityEvents()) {
                    UserManager userManager = (UserManager) AccessibilityManagerService.this.mContext.getSystemService("user");
                    String message = AccessibilityManagerService.this.mContext.getString(17041175, new Object[]{userManager.getUserInfo(AccessibilityManagerService.this.mCurrentUserId).name});
                    AccessibilityEvent event = AccessibilityEvent.obtain(16384);
                    event.getText().add(message);
                    AccessibilityManagerService.this.sendAccessibilityEvent(event, AccessibilityManagerService.this.mCurrentUserId);
                }
            }
        }

        private RemoteCallbackList<IAccessibilityManagerClient> getUserClientsForId(int userId) {
            UserState userState;
            synchronized (AccessibilityManagerService.this.mLock) {
                userState = AccessibilityManagerService.this.getUserStateLocked(userId);
            }
            return userState.mUserClients;
        }

        private void sendStateToClients(int clientState, RemoteCallbackList<IAccessibilityManagerClient> clients) {
            clients.broadcast(new com.android.server.accessibility.-$Lambda$kXhldx_ZxidxR4suyNIbZ545MMw.AnonymousClass1(clientState));
        }

        static /* synthetic */ void lambda$-com_android_server_accessibility_AccessibilityManagerService$MainHandler_115813(int clientState, IAccessibilityManagerClient client) {
            try {
                client.setState(clientState);
            } catch (RemoteException e) {
            }
        }

        private void notifyClientsOfServicesStateChange(RemoteCallbackList<IAccessibilityManagerClient> clients) {
            try {
                int userClientCount = clients.beginBroadcast();
                for (int i = 0; i < userClientCount; i++) {
                    try {
                        ((IAccessibilityManagerClient) clients.getBroadcastItem(i)).notifyServicesStateChanged();
                    } catch (RemoteException e) {
                    }
                }
            } finally {
                clients.finishBroadcast();
            }
        }
    }

    final class SecurityPolicy {
        public static final int INVALID_WINDOW_ID = -1;
        private static final int RETRIEVAL_ALLOWING_EVENT_TYPES = 244159;
        public long mAccessibilityFocusNodeId = 2147483647L;
        public int mAccessibilityFocusedWindowId = -1;
        public int mActiveWindowId = -1;
        public int mFocusedWindowId = -1;
        private boolean mTouchInteractionInProgress;
        public List<AccessibilityWindowInfo> mWindows;
        public SparseArray<AccessibilityWindowInfo> mWindowsById = new SparseArray();

        SecurityPolicy() {
        }

        private boolean canDispatchAccessibilityEventLocked(AccessibilityEvent event) {
            switch (event.getEventType()) {
                case 32:
                case 64:
                case 128:
                case 256:
                case 512:
                case 1024:
                case 16384:
                case DumpState.DUMP_DOMAIN_PREFERRED /*262144*/:
                case DumpState.DUMP_FROZEN /*524288*/:
                case DumpState.DUMP_DEXOPT /*1048576*/:
                case DumpState.DUMP_COMPILER_STATS /*2097152*/:
                case DumpState.DUMP_CHANGES /*4194304*/:
                case 16777216:
                    return true;
                default:
                    return isRetrievalAllowingWindow(event.getWindowId());
            }
        }

        public void clearWindowsLocked() {
            List<AccessibilityWindowInfo> windows = Collections.emptyList();
            int activeWindowId = this.mActiveWindowId;
            updateWindowsLocked(windows);
            this.mActiveWindowId = activeWindowId;
            this.mWindows = null;
        }

        public void updateWindowsLocked(List<AccessibilityWindowInfo> windows) {
            int i;
            if (this.mWindows == null) {
                this.mWindows = new ArrayList();
            }
            for (i = this.mWindows.size() - 1; i >= 0; i--) {
                ((AccessibilityWindowInfo) this.mWindows.remove(i)).recycle();
            }
            this.mWindowsById.clear();
            this.mFocusedWindowId = -1;
            if (!this.mTouchInteractionInProgress) {
                this.mActiveWindowId = -1;
            }
            boolean activeWindowGone = true;
            int windowCount = windows.size();
            if (windowCount > 0) {
                AccessibilityWindowInfo window;
                for (i = 0; i < windowCount; i++) {
                    window = (AccessibilityWindowInfo) windows.get(i);
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
                    this.mWindowsById.put(windowId, window);
                }
                if (this.mTouchInteractionInProgress && activeWindowGone) {
                    this.mActiveWindowId = this.mFocusedWindowId;
                }
                for (i = 0; i < windowCount; i++) {
                    window = (AccessibilityWindowInfo) this.mWindows.get(i);
                    if (window.getId() == this.mActiveWindowId) {
                        window.setActive(true);
                    }
                    if (window.getId() == this.mAccessibilityFocusedWindowId) {
                        window.setAccessibilityFocused(true);
                    }
                }
            }
            notifyWindowsChanged();
        }

        public boolean computePartialInteractiveRegionForWindowLocked(int windowId, Region outRegion) {
            if (this.mWindows == null) {
                return false;
            }
            Region windowInteractiveRegion = null;
            boolean windowInteractiveRegionChanged = false;
            for (int i = this.mWindows.size() - 1; i >= 0; i--) {
                AccessibilityWindowInfo currentWindow = (AccessibilityWindowInfo) this.mWindows.get(i);
                Rect currentWindowBounds;
                if (windowInteractiveRegion == null) {
                    if (currentWindow.getId() == windowId) {
                        currentWindowBounds = AccessibilityManagerService.this.mTempRect;
                        currentWindow.getBoundsInScreen(currentWindowBounds);
                        outRegion.set(currentWindowBounds);
                        windowInteractiveRegion = outRegion;
                    }
                } else if (currentWindow.getType() != 4) {
                    currentWindowBounds = AccessibilityManagerService.this.mTempRect;
                    currentWindow.getBoundsInScreen(currentWindowBounds);
                    if (windowInteractiveRegion.op(currentWindowBounds, Op.DIFFERENCE)) {
                        windowInteractiveRegionChanged = true;
                    }
                }
            }
            return windowInteractiveRegionChanged;
        }

        public void updateEventSourceLocked(AccessibilityEvent event) {
            if ((event.getEventType() & RETRIEVAL_ALLOWING_EVENT_TYPES) == 0) {
                event.setSource((View) null);
            }
        }

        public void updateActiveAndAccessibilityFocusedWindowLocked(int windowId, long nodeId, int eventType, int eventAction) {
            Object -get9;
            switch (eventType) {
                case 32:
                    -get9 = AccessibilityManagerService.this.mLock;
                    synchronized (-get9) {
                        if (AccessibilityManagerService.this.mWindowsForAccessibilityCallback == null) {
                            this.mFocusedWindowId = getFocusedWindowId();
                            if (windowId == this.mFocusedWindowId) {
                                this.mActiveWindowId = windowId;
                                break;
                            }
                        }
                    }
                    break;
                case 128:
                    -get9 = AccessibilityManagerService.this.mLock;
                    synchronized (-get9) {
                        if (this.mTouchInteractionInProgress && this.mActiveWindowId != windowId) {
                            setActiveWindowLocked(windowId);
                            break;
                        }
                    }
                case 32768:
                    -get9 = AccessibilityManagerService.this.mLock;
                    synchronized (-get9) {
                        if (this.mAccessibilityFocusedWindowId != windowId) {
                            AccessibilityManagerService.this.mMainHandler.obtainMessage(9, this.mAccessibilityFocusedWindowId, 0).sendToTarget();
                            AccessibilityManagerService.this.mSecurityPolicy.setAccessibilityFocusedWindowLocked(windowId);
                            this.mAccessibilityFocusNodeId = nodeId;
                            break;
                        }
                    }
                    break;
                case 65536:
                    -get9 = AccessibilityManagerService.this.mLock;
                    synchronized (-get9) {
                        if (this.mAccessibilityFocusNodeId == nodeId) {
                            this.mAccessibilityFocusNodeId = 2147483647L;
                        }
                        if (this.mAccessibilityFocusNodeId == 2147483647L && this.mAccessibilityFocusedWindowId == windowId && eventAction != 64) {
                            this.mAccessibilityFocusedWindowId = -1;
                            break;
                        }
                    }
                default:
                    return;
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
                    AccessibilityManagerService.this.mMainHandler.obtainMessage(9, oldActiveWindow, 0).sendToTarget();
                }
            }
        }

        public int getActiveWindowId() {
            if (this.mActiveWindowId == -1 && (this.mTouchInteractionInProgress ^ 1) != 0) {
                this.mActiveWindowId = getFocusedWindowId();
            }
            return this.mActiveWindowId;
        }

        private void setActiveWindowLocked(int windowId) {
            if (this.mActiveWindowId != windowId) {
                this.mActiveWindowId = windowId;
                if (this.mWindows != null) {
                    int windowCount = this.mWindows.size();
                    for (int i = 0; i < windowCount; i++) {
                        AccessibilityWindowInfo window = (AccessibilityWindowInfo) this.mWindows.get(i);
                        window.setActive(window.getId() == windowId);
                    }
                }
                notifyWindowsChanged();
            }
        }

        private void setAccessibilityFocusedWindowLocked(int windowId) {
            if (this.mAccessibilityFocusedWindowId != windowId) {
                this.mAccessibilityFocusedWindowId = windowId;
                if (this.mWindows != null) {
                    int windowCount = this.mWindows.size();
                    for (int i = 0; i < windowCount; i++) {
                        AccessibilityWindowInfo window = (AccessibilityWindowInfo) this.mWindows.get(i);
                        window.setAccessibilityFocused(window.getId() == windowId);
                    }
                }
                notifyWindowsChanged();
            }
        }

        public void notifyWindowsChanged() {
            if (AccessibilityManagerService.this.mWindowsForAccessibilityCallback != null) {
                long identity = Binder.clearCallingIdentity();
                try {
                    AccessibilityEvent event = AccessibilityEvent.obtain(DumpState.DUMP_CHANGES);
                    event.setEventTime(SystemClock.uptimeMillis());
                    AccessibilityManagerService.this.sendAccessibilityEvent(event, AccessibilityManagerService.this.mCurrentUserId);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        public boolean canGetAccessibilityNodeInfoLocked(Service service, int windowId) {
            return canRetrieveWindowContentLocked(service) ? isRetrievalAllowingWindow(windowId) : false;
        }

        public boolean canRetrieveWindowsLocked(Service service) {
            return canRetrieveWindowContentLocked(service) ? service.mRetrieveInteractiveWindows : false;
        }

        public boolean canRetrieveWindowContentLocked(Service service) {
            return (service.mAccessibilityServiceInfo.getCapabilities() & 1) != 0;
        }

        public boolean canControlMagnification(Service service) {
            return (service.mAccessibilityServiceInfo.getCapabilities() & 16) != 0;
        }

        public boolean canPerformGestures(Service service) {
            return (service.mAccessibilityServiceInfo.getCapabilities() & 32) != 0;
        }

        public boolean canCaptureFingerprintGestures(Service service) {
            return (service.mAccessibilityServiceInfo.getCapabilities() & 64) != 0;
        }

        private int resolveProfileParentLocked(int userId) {
            if (userId != AccessibilityManagerService.this.mCurrentUserId) {
                long identity = Binder.clearCallingIdentity();
                try {
                    UserInfo parent = AccessibilityManagerService.this.mUserManager.getProfileParent(userId);
                    if (parent != null) {
                        int identifier = parent.getUserHandle().getIdentifier();
                        return identifier;
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
            if (callingUid != 0 && callingUid != 1000 && callingUid != IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME) {
                int callingUserId = UserHandle.getUserId(callingUid);
                if (callingUserId == userId) {
                    return resolveProfileParentLocked(userId);
                }
                if (resolveProfileParentLocked(callingUserId) == AccessibilityManagerService.this.mCurrentUserId && (userId == -2 || userId == -3)) {
                    return AccessibilityManagerService.this.mCurrentUserId;
                }
                if (!hasPermission("android.permission.INTERACT_ACROSS_USERS") && (hasPermission("android.permission.INTERACT_ACROSS_USERS_FULL") ^ 1) != 0) {
                    throw new SecurityException("Call from user " + callingUserId + " as user " + userId + " without permission INTERACT_ACROSS_USERS or " + "INTERACT_ACROSS_USERS_FULL not allowed.");
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
            int callingUid = Binder.getCallingUid();
            if (Binder.getCallingPid() == Process.myPid() || callingUid == IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME || userId == -2 || userId == -3) {
                return true;
            }
            return false;
        }

        private boolean isRetrievalAllowingWindow(int windowId) {
            boolean z = true;
            if (Binder.getCallingUid() == 1000 || windowId == this.mActiveWindowId) {
                return true;
            }
            if (findWindowById(windowId) == null) {
                z = false;
            }
            return z;
        }

        private AccessibilityWindowInfo findWindowById(int windowId) {
            return (AccessibilityWindowInfo) this.mWindowsById.get(windowId);
        }

        private AccessibilityWindowInfo getPictureInPictureWindow() {
            if (this.mWindows != null) {
                int windowCount = this.mWindows.size();
                for (int i = 0; i < windowCount; i++) {
                    AccessibilityWindowInfo window = (AccessibilityWindowInfo) this.mWindows.get(i);
                    if (window.inPictureInPicture()) {
                        return window;
                    }
                }
            }
            return null;
        }

        private void enforceCallingPermission(String permission, String function) {
            if (AccessibilityManagerService.OWN_PROCESS_ID != Binder.getCallingPid() && !hasPermission(permission)) {
                throw new SecurityException("You do not have " + permission + " required to call " + function + " from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            }
        }

        private boolean hasPermission(String permission) {
            return AccessibilityManagerService.this.mContext.checkCallingPermission(permission) == 0;
        }

        private int getFocusedWindowId() {
            int -wrap15;
            IBinder token = AccessibilityManagerService.this.mWindowManagerService.getFocusedWindowToken();
            synchronized (AccessibilityManagerService.this.mLock) {
                -wrap15 = AccessibilityManagerService.this.findWindowIdLocked(token);
            }
            return -wrap15;
        }
    }

    class Service extends IAccessibilityServiceConnection.Stub implements ServiceConnection, DeathRecipient, KeyEventFilter, FingerprintGestureClient {
        AccessibilityServiceInfo mAccessibilityServiceInfo;
        boolean mCaptureFingerprintGestures;
        ComponentName mComponentName;
        public Handler mEventDispatchHandler = new Handler(AccessibilityManagerService.this.mMainHandler.getLooper()) {
            public void handleMessage(Message message) {
                Service.this.notifyAccessibilityEventInternal(message.what, message.obj, message.arg1 != 0);
            }
        };
        int mEventTypes;
        int mFeedbackType;
        int mFetchFlags;
        int mId = 0;
        Intent mIntent;
        public final InvocationHandler mInvocationHandler = new InvocationHandler(AccessibilityManagerService.this.mMainHandler.getLooper());
        boolean mIsAutomation;
        boolean mIsDefault;
        boolean mLastAccessibilityButtonCallbackState;
        long mNotificationTimeout;
        final IBinder mOverlayWindowToken = new Binder();
        Set<String> mPackageNames = new HashSet();
        final SparseArray<AccessibilityEvent> mPendingEvents = new SparseArray();
        boolean mReceivedAccessibilityButtonCallbackSinceBind;
        boolean mRequestAccessibilityButton;
        boolean mRequestFilterKeyEvents;
        boolean mRequestTouchExplorationMode;
        final ResolveInfo mResolveInfo;
        boolean mRetrieveInteractiveWindows;
        IBinder mService;
        IAccessibilityServiceClient mServiceInterface;
        final int mUserId;
        boolean mUsesAccessibilityCache = false;
        boolean mWasConnectedAndDied;

        private final class InvocationHandler extends Handler {
            public static final int MSG_CLEAR_ACCESSIBILITY_CACHE = 2;
            private static final int MSG_ON_ACCESSIBILITY_BUTTON_AVAILABILITY_CHANGED = 8;
            private static final int MSG_ON_ACCESSIBILITY_BUTTON_CLICKED = 7;
            public static final int MSG_ON_GESTURE = 1;
            private static final int MSG_ON_MAGNIFICATION_CHANGED = 5;
            private static final int MSG_ON_SOFT_KEYBOARD_STATE_CHANGED = 6;
            private boolean mIsMagnificationCallbackEnabled = false;
            private boolean mIsSoftKeyboardCallbackEnabled = false;

            public InvocationHandler(Looper looper) {
                super(looper, null, true);
            }

            public void handleMessage(Message message) {
                int type = message.what;
                switch (type) {
                    case 1:
                        Service.this.notifyGestureInternal(message.arg1);
                        return;
                    case 2:
                        Service.this.notifyClearAccessibilityCacheInternal();
                        return;
                    case 5:
                        SomeArgs args = message.obj;
                        Service.this.notifyMagnificationChangedInternal(args.arg1, ((Float) args.arg2).floatValue(), ((Float) args.arg3).floatValue(), ((Float) args.arg4).floatValue());
                        return;
                    case 6:
                        Service.this.notifySoftKeyboardShowModeChangedInternal(message.arg1);
                        return;
                    case 7:
                        Service.this.notifyAccessibilityButtonClickedInternal();
                        return;
                    case 8:
                        Service.this.notifyAccessibilityButtonAvailabilityChangedInternal(message.arg1 != 0);
                        return;
                    default:
                        throw new IllegalArgumentException("Unknown message: " + type);
                }
            }

            public void notifyMagnificationChangedLocked(Region region, float scale, float centerX, float centerY) {
                if (this.mIsMagnificationCallbackEnabled) {
                    SomeArgs args = SomeArgs.obtain();
                    args.arg1 = region;
                    args.arg2 = Float.valueOf(scale);
                    args.arg3 = Float.valueOf(centerX);
                    args.arg4 = Float.valueOf(centerY);
                    obtainMessage(5, args).sendToTarget();
                }
            }

            public void setMagnificationCallbackEnabled(boolean enabled) {
                this.mIsMagnificationCallbackEnabled = enabled;
            }

            public void notifySoftKeyboardShowModeChangedLocked(int showState) {
                if (this.mIsSoftKeyboardCallbackEnabled) {
                    obtainMessage(6, showState, 0).sendToTarget();
                }
            }

            public void setSoftKeyboardCallbackEnabled(boolean enabled) {
                this.mIsSoftKeyboardCallbackEnabled = enabled;
            }

            public void notifyAccessibilityButtonClickedLocked() {
                obtainMessage(7).sendToTarget();
            }

            public void notifyAccessibilityButtonAvailabilityChangedLocked(boolean available) {
                int i;
                if (available) {
                    i = 1;
                } else {
                    i = 0;
                }
                obtainMessage(8, i, 0).sendToTarget();
            }
        }

        public Service(int userId, ComponentName componentName, AccessibilityServiceInfo accessibilityServiceInfo) {
            this.mUserId = userId;
            this.mResolveInfo = accessibilityServiceInfo.getResolveInfo();
            int -get22 = AccessibilityManagerService.sIdCounter;
            AccessibilityManagerService.sIdCounter = -get22 + 1;
            this.mId = -get22;
            this.mComponentName = componentName;
            this.mAccessibilityServiceInfo = accessibilityServiceInfo;
            this.mIsAutomation = AccessibilityManagerService.sFakeAccessibilityServiceComponentName.equals(componentName);
            if (!this.mIsAutomation) {
                this.mIntent = new Intent().setComponent(this.mComponentName);
                this.mIntent.putExtra("android.intent.extra.client_label", 17039522);
                long idendtity = Binder.clearCallingIdentity();
                try {
                    this.mIntent.putExtra("android.intent.extra.client_intent", PendingIntent.getActivity(AccessibilityManagerService.this.mContext, 0, new Intent("android.settings.ACCESSIBILITY_SETTINGS"), 0));
                } finally {
                    Binder.restoreCallingIdentity(idendtity);
                }
            }
            setDynamicallyConfigurableProperties(accessibilityServiceInfo);
        }

        /* JADX WARNING: Missing block: B:4:0x0009, code:
            return false;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean onKeyEvent(KeyEvent keyEvent, int sequenceNumber) {
            if (!this.mRequestFilterKeyEvents || this.mServiceInterface == null || (this.mAccessibilityServiceInfo.getCapabilities() & 8) == 0) {
                return false;
            }
            try {
                this.mServiceInterface.onKeyEvent(keyEvent, sequenceNumber);
                return true;
            } catch (RemoteException e) {
                return false;
            }
        }

        public boolean isCapturingFingerprintGestures() {
            if (this.mServiceInterface == null || !AccessibilityManagerService.this.mSecurityPolicy.canCaptureFingerprintGestures(this)) {
                return false;
            }
            return this.mCaptureFingerprintGestures;
        }

        public void onFingerprintGestureDetectionActiveChanged(boolean active) {
            if (isCapturingFingerprintGestures()) {
                IAccessibilityServiceClient serviceInterface;
                synchronized (AccessibilityManagerService.this.mLock) {
                    serviceInterface = this.mServiceInterface;
                }
                if (serviceInterface != null) {
                    try {
                        this.mServiceInterface.onFingerprintCapturingGesturesChanged(active);
                    } catch (RemoteException e) {
                    }
                }
            }
        }

        public void onFingerprintGesture(int gesture) {
            if (isCapturingFingerprintGestures()) {
                IAccessibilityServiceClient serviceInterface;
                synchronized (AccessibilityManagerService.this.mLock) {
                    serviceInterface = this.mServiceInterface;
                }
                if (serviceInterface != null) {
                    try {
                        this.mServiceInterface.onFingerprintGesture(gesture);
                    } catch (RemoteException e) {
                    }
                }
            }
        }

        public void setDynamicallyConfigurableProperties(AccessibilityServiceInfo info) {
            boolean z;
            boolean z2 = true;
            this.mEventTypes = info.eventTypes;
            this.mFeedbackType = info.feedbackType;
            String[] packageNames = info.packageNames;
            if (packageNames != null) {
                this.mPackageNames.addAll(Arrays.asList(packageNames));
            }
            this.mNotificationTimeout = info.notificationTimeout;
            this.mIsDefault = (info.flags & 1) != 0;
            if (this.mIsAutomation || info.getResolveInfo().serviceInfo.applicationInfo.targetSdkVersion >= 16) {
                if ((info.flags & 2) != 0) {
                    this.mFetchFlags |= 8;
                } else {
                    this.mFetchFlags &= -9;
                }
            }
            if ((info.flags & 16) != 0) {
                this.mFetchFlags |= 16;
            } else {
                this.mFetchFlags &= -17;
            }
            if ((info.flags & 4) != 0) {
                z = true;
            } else {
                z = false;
            }
            this.mRequestTouchExplorationMode = z;
            if ((info.flags & 32) != 0) {
                z = true;
            } else {
                z = false;
            }
            this.mRequestFilterKeyEvents = z;
            if ((info.flags & 64) != 0) {
                z = true;
            } else {
                z = false;
            }
            this.mRetrieveInteractiveWindows = z;
            if ((info.flags & 512) != 0) {
                z = true;
            } else {
                z = false;
            }
            this.mCaptureFingerprintGestures = z;
            if ((info.flags & 256) == 0) {
                z2 = false;
            }
            this.mRequestAccessibilityButton = z2;
        }

        public boolean bindLocked() {
            final UserState userState = AccessibilityManagerService.this.getUserStateLocked(this.mUserId);
            if (this.mIsAutomation) {
                userState.mBindingServices.add(this.mComponentName);
                AccessibilityManagerService.this.mMainHandler.post(new Runnable() {
                    public void run() {
                        Service.this.onServiceConnected(Service.this.mComponentName, userState.mUiAutomationServiceClient.asBinder());
                    }
                });
                userState.mUiAutomationService = this;
            } else {
                long identity = Binder.clearCallingIdentity();
                try {
                    if (this.mService == null && AccessibilityManagerService.this.mContext.bindServiceAsUser(this.mIntent, this, 33554433, new UserHandle(this.mUserId))) {
                        userState.mBindingServices.add(this.mComponentName);
                    }
                    Binder.restoreCallingIdentity(identity);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identity);
                }
            }
            return false;
        }

        public boolean unbindLocked() {
            UserState userState = AccessibilityManagerService.this.getUserStateLocked(this.mUserId);
            AccessibilityManagerService.this.getKeyEventDispatcher().flush(this);
            if (this.mIsAutomation) {
                userState.destroyUiAutomationService();
            } else {
                AccessibilityManagerService.this.mContext.unbindService(this);
            }
            AccessibilityManagerService.this.removeServiceLocked(this, userState);
            resetLocked();
            return true;
        }

        public void disableSelf() {
            synchronized (AccessibilityManagerService.this.mLock) {
                UserState userState = AccessibilityManagerService.this.getUserStateLocked(this.mUserId);
                if (userState.mEnabledServices.remove(this.mComponentName)) {
                    long identity = Binder.clearCallingIdentity();
                    try {
                        AccessibilityManagerService.this.persistComponentNamesToSettingLocked("enabled_accessibility_services", userState.mEnabledServices, this.mUserId);
                        Binder.restoreCallingIdentity(identity);
                        AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(identity);
                    }
                }
            }
        }

        public boolean canReceiveEventsLocked() {
            return (this.mEventTypes == 0 || this.mFeedbackType == 0 || this.mService == null) ? false : true;
        }

        public void setOnKeyEventResult(boolean handled, int sequence) {
            AccessibilityManagerService.this.getKeyEventDispatcher().setOnKeyEventResult(this, handled, sequence);
        }

        public AccessibilityServiceInfo getServiceInfo() {
            AccessibilityServiceInfo accessibilityServiceInfo;
            synchronized (AccessibilityManagerService.this.mLock) {
                accessibilityServiceInfo = this.mAccessibilityServiceInfo;
            }
            return accessibilityServiceInfo;
        }

        public boolean canRetrieveInteractiveWindowsLocked() {
            if (AccessibilityManagerService.this.mSecurityPolicy.canRetrieveWindowContentLocked(this)) {
                return this.mRetrieveInteractiveWindows;
            }
            return false;
        }

        public void setServiceInfo(AccessibilityServiceInfo info) {
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (AccessibilityManagerService.this.mLock) {
                    AccessibilityServiceInfo oldInfo = this.mAccessibilityServiceInfo;
                    if (oldInfo != null) {
                        oldInfo.updateDynamicallyConfigurableProperties(info);
                        setDynamicallyConfigurableProperties(oldInfo);
                    } else {
                        setDynamicallyConfigurableProperties(info);
                    }
                    UserState userState = AccessibilityManagerService.this.getUserStateLocked(this.mUserId);
                    AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                    AccessibilityManagerService.this.scheduleNotifyClientsOfServicesStateChange(userState);
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void onServiceConnected(ComponentName componentName, IBinder service) {
            synchronized (AccessibilityManagerService.this.mLock) {
                if (this.mService != service) {
                    if (this.mService != null) {
                        this.mService.unlinkToDeath(this, 0);
                    }
                    this.mService = service;
                    try {
                        this.mService.linkToDeath(this, 0);
                    } catch (RemoteException e) {
                        Slog.e(AccessibilityManagerService.LOG_TAG, "Failed registering death link");
                        binderDied();
                        return;
                    }
                }
                this.mServiceInterface = IAccessibilityServiceClient.Stub.asInterface(service);
                UserState userState = AccessibilityManagerService.this.getUserStateLocked(this.mUserId);
                AccessibilityManagerService.this.addServiceLocked(this, userState);
                if (userState.mBindingServices.contains(this.mComponentName) || this.mWasConnectedAndDied) {
                    userState.mBindingServices.remove(this.mComponentName);
                    this.mWasConnectedAndDied = false;
                    try {
                        this.mServiceInterface.init(this, this.mId, this.mOverlayWindowToken);
                        AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                    } catch (RemoteException re) {
                        Slog.w(AccessibilityManagerService.LOG_TAG, "Error while setting connection for service: " + service, re);
                        binderDied();
                    }
                } else {
                    binderDied();
                }
            }
            return;
        }

        private boolean isCalledForCurrentUserLocked() {
            return AccessibilityManagerService.this.mSecurityPolicy.resolveCallingUserIdEnforcingPermissionsLocked(-2) == AccessibilityManagerService.this.mCurrentUserId;
        }

        public List<AccessibilityWindowInfo> getWindows() {
            AccessibilityManagerService.this.ensureWindowsAvailableTimed();
            synchronized (AccessibilityManagerService.this.mLock) {
                if (!isCalledForCurrentUserLocked()) {
                    return null;
                } else if (!AccessibilityManagerService.this.mSecurityPolicy.canRetrieveWindowsLocked(this)) {
                    return null;
                } else if (AccessibilityManagerService.this.mSecurityPolicy.mWindows == null) {
                    return null;
                } else {
                    List<AccessibilityWindowInfo> windows = new ArrayList();
                    int windowCount = AccessibilityManagerService.this.mSecurityPolicy.mWindows.size();
                    for (int i = 0; i < windowCount; i++) {
                        AccessibilityWindowInfo windowClone = AccessibilityWindowInfo.obtain((AccessibilityWindowInfo) AccessibilityManagerService.this.mSecurityPolicy.mWindows.get(i));
                        windowClone.setConnectionId(this.mId);
                        windows.add(windowClone);
                    }
                    return windows;
                }
            }
        }

        public AccessibilityWindowInfo getWindow(int windowId) {
            AccessibilityManagerService.this.ensureWindowsAvailableTimed();
            synchronized (AccessibilityManagerService.this.mLock) {
                if (!isCalledForCurrentUserLocked()) {
                    return null;
                } else if (AccessibilityManagerService.this.mSecurityPolicy.canRetrieveWindowsLocked(this)) {
                    AccessibilityWindowInfo window = AccessibilityManagerService.this.mSecurityPolicy.findWindowById(windowId);
                    if (window != null) {
                        AccessibilityWindowInfo windowClone = AccessibilityWindowInfo.obtain(window);
                        windowClone.setConnectionId(this.mId);
                        return windowClone;
                    }
                    return null;
                } else {
                    return null;
                }
            }
        }

        /* JADX WARNING: Missing block: B:27:0x0059, code:
            r7 = android.os.Binder.getCallingPid();
            r31 = replaceCallbackIfNeeded(r31, r5, r30, r7, r32);
            r22 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:29:?, code:
            r2.findAccessibilityNodeInfosByViewId(r27, r29, r13, r30, r31, r25.mFetchFlags, r7, r32, r20);
     */
        /* JADX WARNING: Missing block: B:30:0x0083, code:
            android.os.Binder.restoreCallingIdentity(r22);
     */
        /* JADX WARNING: Missing block: B:31:0x0087, code:
            if (r13 == null) goto L_0x0092;
     */
        /* JADX WARNING: Missing block: B:33:0x008d, code:
            if (android.os.Binder.isProxy(r2) == false) goto L_0x0092;
     */
        /* JADX WARNING: Missing block: B:34:0x008f, code:
            r13.recycle();
     */
        /* JADX WARNING: Missing block: B:35:0x0092, code:
            return true;
     */
        /* JADX WARNING: Missing block: B:40:0x0097, code:
            android.os.Binder.restoreCallingIdentity(r22);
     */
        /* JADX WARNING: Missing block: B:44:0x00a2, code:
            r13.recycle();
     */
        /* JADX WARNING: Missing block: B:46:0x00a6, code:
            return false;
     */
        /* JADX WARNING: Missing block: B:47:0x00a7, code:
            r3 = move-exception;
     */
        /* JADX WARNING: Missing block: B:48:0x00a8, code:
            android.os.Binder.restoreCallingIdentity(r22);
     */
        /* JADX WARNING: Missing block: B:52:0x00b3, code:
            r13.recycle();
     */
        /* JADX WARNING: Missing block: B:53:0x00b6, code:
            throw r3;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean findAccessibilityNodeInfosByViewId(int accessibilityWindowId, long accessibilityNodeId, String viewIdResName, int interactionId, IAccessibilityInteractionConnectionCallback callback, long interrogatingTid) throws RemoteException {
            Region partialInteractiveRegion = Region.obtain();
            synchronized (AccessibilityManagerService.this.mLock) {
                try {
                    this.mUsesAccessibilityCache = true;
                    if (isCalledForCurrentUserLocked()) {
                        int resolvedWindowId = resolveAccessibilityWindowIdLocked(accessibilityWindowId);
                        if (AccessibilityManagerService.this.mSecurityPolicy.canGetAccessibilityNodeInfoLocked(this, resolvedWindowId)) {
                            IAccessibilityInteractionConnection connection = getConnectionLocked(resolvedWindowId);
                            if (connection == null) {
                                return false;
                            }
                            if (!AccessibilityManagerService.this.mSecurityPolicy.computePartialInteractiveRegionForWindowLocked(resolvedWindowId, partialInteractiveRegion)) {
                                partialInteractiveRegion.recycle();
                                partialInteractiveRegion = null;
                            }
                            MagnificationSpec spec = AccessibilityManagerService.this.getCompatibleMagnificationSpecLocked(resolvedWindowId);
                        } else {
                            return false;
                        }
                    }
                    return false;
                } catch (Throwable th) {
                    throw th;
                }
            }
        }

        /* JADX WARNING: Missing block: B:27:0x0059, code:
            r7 = android.os.Binder.getCallingPid();
            r31 = replaceCallbackIfNeeded(r31, r5, r30, r7, r32);
            r22 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:29:?, code:
            r2.findAccessibilityNodeInfosByText(r27, r29, r13, r30, r31, r25.mFetchFlags, r7, r32, r20);
     */
        /* JADX WARNING: Missing block: B:30:0x0083, code:
            android.os.Binder.restoreCallingIdentity(r22);
     */
        /* JADX WARNING: Missing block: B:31:0x0087, code:
            if (r13 == null) goto L_0x0092;
     */
        /* JADX WARNING: Missing block: B:33:0x008d, code:
            if (android.os.Binder.isProxy(r2) == false) goto L_0x0092;
     */
        /* JADX WARNING: Missing block: B:34:0x008f, code:
            r13.recycle();
     */
        /* JADX WARNING: Missing block: B:35:0x0092, code:
            return true;
     */
        /* JADX WARNING: Missing block: B:40:0x0097, code:
            android.os.Binder.restoreCallingIdentity(r22);
     */
        /* JADX WARNING: Missing block: B:44:0x00a2, code:
            r13.recycle();
     */
        /* JADX WARNING: Missing block: B:46:0x00a6, code:
            return false;
     */
        /* JADX WARNING: Missing block: B:47:0x00a7, code:
            r3 = move-exception;
     */
        /* JADX WARNING: Missing block: B:48:0x00a8, code:
            android.os.Binder.restoreCallingIdentity(r22);
     */
        /* JADX WARNING: Missing block: B:52:0x00b3, code:
            r13.recycle();
     */
        /* JADX WARNING: Missing block: B:53:0x00b6, code:
            throw r3;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean findAccessibilityNodeInfosByText(int accessibilityWindowId, long accessibilityNodeId, String text, int interactionId, IAccessibilityInteractionConnectionCallback callback, long interrogatingTid) throws RemoteException {
            Region partialInteractiveRegion = Region.obtain();
            synchronized (AccessibilityManagerService.this.mLock) {
                try {
                    this.mUsesAccessibilityCache = true;
                    if (isCalledForCurrentUserLocked()) {
                        int resolvedWindowId = resolveAccessibilityWindowIdLocked(accessibilityWindowId);
                        if (AccessibilityManagerService.this.mSecurityPolicy.canGetAccessibilityNodeInfoLocked(this, resolvedWindowId)) {
                            IAccessibilityInteractionConnection connection = getConnectionLocked(resolvedWindowId);
                            if (connection == null) {
                                return false;
                            }
                            if (!AccessibilityManagerService.this.mSecurityPolicy.computePartialInteractiveRegionForWindowLocked(resolvedWindowId, partialInteractiveRegion)) {
                                partialInteractiveRegion.recycle();
                                partialInteractiveRegion = null;
                            }
                            MagnificationSpec spec = AccessibilityManagerService.this.getCompatibleMagnificationSpecLocked(resolvedWindowId);
                        } else {
                            return false;
                        }
                    }
                    return false;
                } catch (Throwable th) {
                    throw th;
                }
            }
        }

        /* JADX WARNING: Missing block: B:27:0x0059, code:
            r7 = android.os.Binder.getCallingPid();
            r30 = replaceCallbackIfNeeded(r30, r5, r29, r7, r32);
            r20 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:29:?, code:
            r2.findAccessibilityNodeInfoByAccessibilityId(r27, r11, r29, r30, r25.mFetchFlags | r31, r7, r32, r18, r34);
     */
        /* JADX WARNING: Missing block: B:30:0x0082, code:
            android.os.Binder.restoreCallingIdentity(r20);
     */
        /* JADX WARNING: Missing block: B:31:0x0086, code:
            if (r11 == null) goto L_0x0091;
     */
        /* JADX WARNING: Missing block: B:33:0x008c, code:
            if (android.os.Binder.isProxy(r2) == false) goto L_0x0091;
     */
        /* JADX WARNING: Missing block: B:34:0x008e, code:
            r11.recycle();
     */
        /* JADX WARNING: Missing block: B:35:0x0091, code:
            return true;
     */
        /* JADX WARNING: Missing block: B:40:0x0096, code:
            android.os.Binder.restoreCallingIdentity(r20);
     */
        /* JADX WARNING: Missing block: B:44:0x00a1, code:
            r11.recycle();
     */
        /* JADX WARNING: Missing block: B:46:0x00a5, code:
            return false;
     */
        /* JADX WARNING: Missing block: B:47:0x00a6, code:
            r3 = move-exception;
     */
        /* JADX WARNING: Missing block: B:48:0x00a7, code:
            android.os.Binder.restoreCallingIdentity(r20);
     */
        /* JADX WARNING: Missing block: B:52:0x00b2, code:
            r11.recycle();
     */
        /* JADX WARNING: Missing block: B:53:0x00b5, code:
            throw r3;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean findAccessibilityNodeInfoByAccessibilityId(int accessibilityWindowId, long accessibilityNodeId, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, long interrogatingTid, Bundle arguments) throws RemoteException {
            Region partialInteractiveRegion = Region.obtain();
            synchronized (AccessibilityManagerService.this.mLock) {
                try {
                    this.mUsesAccessibilityCache = true;
                    if (isCalledForCurrentUserLocked()) {
                        int resolvedWindowId = resolveAccessibilityWindowIdLocked(accessibilityWindowId);
                        if (AccessibilityManagerService.this.mSecurityPolicy.canGetAccessibilityNodeInfoLocked(this, resolvedWindowId)) {
                            IAccessibilityInteractionConnection connection = getConnectionLocked(resolvedWindowId);
                            if (connection == null) {
                                return false;
                            }
                            if (!AccessibilityManagerService.this.mSecurityPolicy.computePartialInteractiveRegionForWindowLocked(resolvedWindowId, partialInteractiveRegion)) {
                                partialInteractiveRegion.recycle();
                                partialInteractiveRegion = null;
                            }
                            MagnificationSpec spec = AccessibilityManagerService.this.getCompatibleMagnificationSpecLocked(resolvedWindowId);
                        } else {
                            return false;
                        }
                    }
                    return false;
                } catch (Throwable th) {
                    throw th;
                }
            }
        }

        /* JADX WARNING: Missing block: B:26:0x005a, code:
            r9 = android.os.Binder.getCallingPid();
            r33 = replaceCallbackIfNeeded(r33, r7, r32, r9, r34);
            r24 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:28:?, code:
            r4.findFocus(r29, r31, r15, r32, r33, r27.mFetchFlags, r9, r34, r22);
     */
        /* JADX WARNING: Missing block: B:29:0x0084, code:
            android.os.Binder.restoreCallingIdentity(r24);
     */
        /* JADX WARNING: Missing block: B:30:0x0088, code:
            if (r15 == null) goto L_0x0093;
     */
        /* JADX WARNING: Missing block: B:32:0x008e, code:
            if (android.os.Binder.isProxy(r4) == false) goto L_0x0093;
     */
        /* JADX WARNING: Missing block: B:33:0x0090, code:
            r15.recycle();
     */
        /* JADX WARNING: Missing block: B:34:0x0093, code:
            return true;
     */
        /* JADX WARNING: Missing block: B:39:0x0098, code:
            android.os.Binder.restoreCallingIdentity(r24);
     */
        /* JADX WARNING: Missing block: B:43:0x00a3, code:
            r15.recycle();
     */
        /* JADX WARNING: Missing block: B:45:0x00a7, code:
            return false;
     */
        /* JADX WARNING: Missing block: B:46:0x00a8, code:
            r5 = move-exception;
     */
        /* JADX WARNING: Missing block: B:47:0x00a9, code:
            android.os.Binder.restoreCallingIdentity(r24);
     */
        /* JADX WARNING: Missing block: B:51:0x00b4, code:
            r15.recycle();
     */
        /* JADX WARNING: Missing block: B:52:0x00b7, code:
            throw r5;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean findFocus(int accessibilityWindowId, long accessibilityNodeId, int focusType, int interactionId, IAccessibilityInteractionConnectionCallback callback, long interrogatingTid) throws RemoteException {
            Region partialInteractiveRegion = Region.obtain();
            synchronized (AccessibilityManagerService.this.mLock) {
                try {
                    if (isCalledForCurrentUserLocked()) {
                        int resolvedWindowId = resolveAccessibilityWindowIdForFindFocusLocked(accessibilityWindowId, focusType);
                        if (AccessibilityManagerService.this.mSecurityPolicy.canGetAccessibilityNodeInfoLocked(this, resolvedWindowId)) {
                            IAccessibilityInteractionConnection connection = getConnectionLocked(resolvedWindowId);
                            if (connection == null) {
                                return false;
                            }
                            if (!AccessibilityManagerService.this.mSecurityPolicy.computePartialInteractiveRegionForWindowLocked(resolvedWindowId, partialInteractiveRegion)) {
                                partialInteractiveRegion.recycle();
                                partialInteractiveRegion = null;
                            }
                            MagnificationSpec spec = AccessibilityManagerService.this.getCompatibleMagnificationSpecLocked(resolvedWindowId);
                        } else {
                            return false;
                        }
                    }
                    return false;
                } catch (Throwable th) {
                    throw th;
                }
            }
        }

        /* JADX WARNING: Missing block: B:26:0x0054, code:
            r7 = android.os.Binder.getCallingPid();
            r31 = replaceCallbackIfNeeded(r31, r5, r30, r7, r32);
            r22 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:28:?, code:
            r2.focusSearch(r27, r29, r13, r30, r31, r25.mFetchFlags, r7, r32, r20);
     */
        /* JADX WARNING: Missing block: B:29:0x007e, code:
            android.os.Binder.restoreCallingIdentity(r22);
     */
        /* JADX WARNING: Missing block: B:30:0x0082, code:
            if (r13 == null) goto L_0x008d;
     */
        /* JADX WARNING: Missing block: B:32:0x0088, code:
            if (android.os.Binder.isProxy(r2) == false) goto L_0x008d;
     */
        /* JADX WARNING: Missing block: B:33:0x008a, code:
            r13.recycle();
     */
        /* JADX WARNING: Missing block: B:34:0x008d, code:
            return true;
     */
        /* JADX WARNING: Missing block: B:39:0x0092, code:
            android.os.Binder.restoreCallingIdentity(r22);
     */
        /* JADX WARNING: Missing block: B:43:0x009d, code:
            r13.recycle();
     */
        /* JADX WARNING: Missing block: B:45:0x00a1, code:
            return false;
     */
        /* JADX WARNING: Missing block: B:46:0x00a2, code:
            r3 = move-exception;
     */
        /* JADX WARNING: Missing block: B:47:0x00a3, code:
            android.os.Binder.restoreCallingIdentity(r22);
     */
        /* JADX WARNING: Missing block: B:51:0x00ae, code:
            r13.recycle();
     */
        /* JADX WARNING: Missing block: B:52:0x00b1, code:
            throw r3;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean focusSearch(int accessibilityWindowId, long accessibilityNodeId, int direction, int interactionId, IAccessibilityInteractionConnectionCallback callback, long interrogatingTid) throws RemoteException {
            Region partialInteractiveRegion = Region.obtain();
            synchronized (AccessibilityManagerService.this.mLock) {
                try {
                    if (isCalledForCurrentUserLocked()) {
                        int resolvedWindowId = resolveAccessibilityWindowIdLocked(accessibilityWindowId);
                        if (AccessibilityManagerService.this.mSecurityPolicy.canGetAccessibilityNodeInfoLocked(this, resolvedWindowId)) {
                            IAccessibilityInteractionConnection connection = getConnectionLocked(resolvedWindowId);
                            if (connection == null) {
                                return false;
                            }
                            if (!AccessibilityManagerService.this.mSecurityPolicy.computePartialInteractiveRegionForWindowLocked(resolvedWindowId, partialInteractiveRegion)) {
                                partialInteractiveRegion.recycle();
                                partialInteractiveRegion = null;
                            }
                            MagnificationSpec spec = AccessibilityManagerService.this.getCompatibleMagnificationSpecLocked(resolvedWindowId);
                        } else {
                            return false;
                        }
                    }
                    return false;
                } catch (Throwable th) {
                    throw th;
                }
            }
        }

        /* JADX WARNING: Missing block: B:26:?, code:
            r12.mServiceInterface.onPerformGestureResult(r13, false);
     */
        /* JADX WARNING: Missing block: B:31:0x006a, code:
            r3 = move-exception;
     */
        /* JADX WARNING: Missing block: B:32:0x006b, code:
            android.util.Slog.e(com.android.server.accessibility.AccessibilityManagerService.LOG_TAG, "Error sending motion event injection failure to " + r12.mServiceInterface, r3);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void sendGesture(int sequence, ParceledListSlice gestureSteps) {
            synchronized (AccessibilityManagerService.this.mLock) {
                if (AccessibilityManagerService.this.mSecurityPolicy.canPerformGestures(this)) {
                    long endMillis = SystemClock.uptimeMillis() + 1000;
                    while (AccessibilityManagerService.this.mMotionEventInjector == null && SystemClock.uptimeMillis() < endMillis) {
                        try {
                            AccessibilityManagerService.this.mLock.wait(endMillis - SystemClock.uptimeMillis());
                        } catch (InterruptedException e) {
                        }
                    }
                    if (AccessibilityManagerService.this.mMotionEventInjector != null) {
                        AccessibilityManagerService.this.mMotionEventInjector.injectEvents(gestureSteps.getList(), this.mServiceInterface, sequence);
                        return;
                    }
                    Slog.e(AccessibilityManagerService.LOG_TAG, "MotionEventInjector installation timed out");
                }
            }
        }

        /* JADX WARNING: Missing block: B:36:0x0077, code:
            r11 = android.os.Binder.getCallingPid();
            r14 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:38:?, code:
            com.android.server.accessibility.AccessibilityManagerService.-get13(r20.this$0).userActivity(android.os.SystemClock.uptimeMillis(), 3, 0);
            r3.performAccessibilityAction(r22, r24, r25, r26, r27, r20.mFetchFlags, r11, r28);
     */
        /* JADX WARNING: Missing block: B:40:0x00a7, code:
            return true;
     */
        /* JADX WARNING: Missing block: B:48:0x00b5, code:
            android.os.Binder.restoreCallingIdentity(r14);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean performAccessibilityAction(int accessibilityWindowId, long accessibilityNodeId, int action, Bundle arguments, int interactionId, IAccessibilityInteractionConnectionCallback callback, long interrogatingTid) throws RemoteException {
            synchronized (AccessibilityManagerService.this.mLock) {
                if (isCalledForCurrentUserLocked()) {
                    int resolvedWindowId = resolveAccessibilityWindowIdLocked(accessibilityWindowId);
                    if (AccessibilityManagerService.this.mSecurityPolicy.canGetAccessibilityNodeInfoLocked(this, resolvedWindowId)) {
                        IAccessibilityInteractionConnection connection = getConnectionLocked(resolvedWindowId);
                        if (connection == null) {
                            return false;
                        }
                        AccessibilityWindowInfo windowInfo = AccessibilityManagerService.this.mSecurityPolicy.findWindowById(resolvedWindowId);
                        if (windowInfo != null && windowInfo.inPictureInPicture()) {
                            boolean isA11yFocusAction = action != 64 ? action == 128 : true;
                            if (!(AccessibilityManagerService.this.mPictureInPictureActionReplacingConnection == null || (isA11yFocusAction ^ 1) == 0)) {
                                connection = AccessibilityManagerService.this.mPictureInPictureActionReplacingConnection.mConnection;
                            }
                        }
                    } else {
                        return false;
                    }
                }
                return false;
            }
        }

        /* JADX WARNING: Missing block: B:8:0x0012, code:
            r0 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:10:?, code:
            com.android.server.accessibility.AccessibilityManagerService.-get13(r9.this$0).userActivity(android.os.SystemClock.uptimeMillis(), 3, 0);
     */
        /* JADX WARNING: Missing block: B:11:0x0025, code:
            switch(r10) {
                case 1: goto L_0x002f;
                case 2: goto L_0x0037;
                case 3: goto L_0x003f;
                case 4: goto L_0x0047;
                case 5: goto L_0x004e;
                case 6: goto L_0x0055;
                case 7: goto L_0x005c;
                default: goto L_0x0028;
            };
     */
        /* JADX WARNING: Missing block: B:18:?, code:
            sendDownAndUpKeyEvents(4);
     */
        /* JADX WARNING: Missing block: B:19:0x0033, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* JADX WARNING: Missing block: B:20:0x0036, code:
            return true;
     */
        /* JADX WARNING: Missing block: B:23:?, code:
            sendDownAndUpKeyEvents(3);
     */
        /* JADX WARNING: Missing block: B:24:0x003b, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* JADX WARNING: Missing block: B:25:0x003e, code:
            return true;
     */
        /* JADX WARNING: Missing block: B:27:?, code:
            r2 = openRecents();
     */
        /* JADX WARNING: Missing block: B:28:0x0042, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* JADX WARNING: Missing block: B:29:0x0046, code:
            return r2;
     */
        /* JADX WARNING: Missing block: B:31:?, code:
            expandNotifications();
     */
        /* JADX WARNING: Missing block: B:32:0x004a, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* JADX WARNING: Missing block: B:33:0x004d, code:
            return true;
     */
        /* JADX WARNING: Missing block: B:35:?, code:
            expandQuickSettings();
     */
        /* JADX WARNING: Missing block: B:36:0x0051, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* JADX WARNING: Missing block: B:37:0x0054, code:
            return true;
     */
        /* JADX WARNING: Missing block: B:39:?, code:
            showGlobalActions();
     */
        /* JADX WARNING: Missing block: B:40:0x0058, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* JADX WARNING: Missing block: B:41:0x005b, code:
            return true;
     */
        /* JADX WARNING: Missing block: B:43:?, code:
            toggleSplitScreen();
     */
        /* JADX WARNING: Missing block: B:44:0x005f, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* JADX WARNING: Missing block: B:45:0x0062, code:
            return true;
     */
        /* JADX WARNING: Missing block: B:47:0x0064, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean performGlobalAction(int action) {
            synchronized (AccessibilityManagerService.this.mLock) {
                if (!isCalledForCurrentUserLocked()) {
                    return false;
                }
            }
            return false;
        }

        public boolean isFingerprintGestureDetectionAvailable() {
            if (!isCapturingFingerprintGestures() || AccessibilityManagerService.this.mFingerprintGestureDispatcher == null) {
                return false;
            }
            return AccessibilityManagerService.this.mFingerprintGestureDispatcher.isFingerprintGestureDetectionAvailable();
        }

        /* JADX WARNING: Missing block: B:9:0x0012, code:
            r0 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:11:?, code:
            r2 = r4.this$0.getMagnificationController().getScale();
     */
        /* JADX WARNING: Missing block: B:17:0x0028, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public float getMagnificationScale() {
            synchronized (AccessibilityManagerService.this.mLock) {
                if (!isCalledForCurrentUserLocked()) {
                    return 1.0f;
                }
            }
            return r2;
        }

        /* JADX WARNING: Missing block: B:21:0x0040, code:
            return r5;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public Region getMagnificationRegion() {
            synchronized (AccessibilityManagerService.this.mLock) {
                Region region = Region.obtain();
                if (isCalledForCurrentUserLocked()) {
                    MagnificationController magnificationController = AccessibilityManagerService.this.getMagnificationController();
                    boolean forceRegistration = AccessibilityManagerService.this.mSecurityPolicy.canControlMagnification(this);
                    boolean initiallyRegistered = magnificationController.isRegisteredLocked();
                    if (!initiallyRegistered && forceRegistration) {
                        magnificationController.register();
                    }
                    long identity = Binder.clearCallingIdentity();
                    try {
                        magnificationController.getMagnificationRegion(region);
                        Binder.restoreCallingIdentity(identity);
                        if (!initiallyRegistered && forceRegistration) {
                            magnificationController.unregister();
                        }
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(identity);
                        if (!initiallyRegistered && forceRegistration) {
                            magnificationController.unregister();
                        }
                    }
                } else {
                    return region;
                }
            }
        }

        /* JADX WARNING: Missing block: B:9:0x0011, code:
            r0 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:11:?, code:
            r2 = r4.this$0.getMagnificationController().getCenterX();
     */
        /* JADX WARNING: Missing block: B:17:0x0027, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public float getMagnificationCenterX() {
            synchronized (AccessibilityManagerService.this.mLock) {
                if (!isCalledForCurrentUserLocked()) {
                    return 0.0f;
                }
            }
            return r2;
        }

        /* JADX WARNING: Missing block: B:9:0x0011, code:
            r0 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:11:?, code:
            r2 = r4.this$0.getMagnificationController().getCenterY();
     */
        /* JADX WARNING: Missing block: B:17:0x0027, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public float getMagnificationCenterY() {
            synchronized (AccessibilityManagerService.this.mLock) {
                if (!isCalledForCurrentUserLocked()) {
                    return 0.0f;
                }
            }
            return r2;
        }

        /* JADX WARNING: Missing block: B:13:0x001f, code:
            r0 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:15:?, code:
            r3 = r6.this$0.getMagnificationController().reset(r7);
     */
        /* JADX WARNING: Missing block: B:21:0x0035, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean resetMagnification(boolean animate) {
            synchronized (AccessibilityManagerService.this.mLock) {
                if (!isCalledForCurrentUserLocked()) {
                    return false;
                } else if (!AccessibilityManagerService.this.mSecurityPolicy.canControlMagnification(this)) {
                    return false;
                }
            }
            return r3;
        }

        public boolean setMagnificationScaleAndCenter(float scale, float centerX, float centerY, boolean animate) {
            synchronized (AccessibilityManagerService.this.mLock) {
                if (!isCalledForCurrentUserLocked()) {
                    return false;
                } else if (AccessibilityManagerService.this.mSecurityPolicy.canControlMagnification(this)) {
                    long identity = Binder.clearCallingIdentity();
                    try {
                        MagnificationController magnificationController = AccessibilityManagerService.this.getMagnificationController();
                        if (!magnificationController.isRegisteredLocked()) {
                            magnificationController.register();
                        }
                        boolean scaleAndCenter = magnificationController.setScaleAndCenter(scale, centerX, centerY, animate, this.mId);
                        Binder.restoreCallingIdentity(identity);
                        return scaleAndCenter;
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(identity);
                    }
                } else {
                    return false;
                }
            }
        }

        public void setMagnificationCallbackEnabled(boolean enabled) {
            this.mInvocationHandler.setMagnificationCallbackEnabled(enabled);
        }

        /* JADX WARNING: Missing block: B:10:0x0017, code:
            r0 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:11:0x001b, code:
            if (r7 != 0) goto L_0x003a;
     */
        /* JADX WARNING: Missing block: B:14:?, code:
            r2.mServiceChangingSoftKeyboardMode = null;
     */
        /* JADX WARNING: Missing block: B:15:0x0020, code:
            android.provider.Settings.Secure.putIntForUser(com.android.server.accessibility.AccessibilityManagerService.-get1(r6.this$0).getContentResolver(), "accessibility_soft_keyboard_mode", r7, r2.mUserId);
     */
        /* JADX WARNING: Missing block: B:16:0x0032, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* JADX WARNING: Missing block: B:17:0x0036, code:
            return true;
     */
        /* JADX WARNING: Missing block: B:22:?, code:
            r2.mServiceChangingSoftKeyboardMode = r6.mComponentName;
     */
        /* JADX WARNING: Missing block: B:24:0x0040, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean setSoftKeyboardShowMode(int showMode) {
            synchronized (AccessibilityManagerService.this.mLock) {
                if (isCalledForCurrentUserLocked()) {
                    UserState userState = AccessibilityManagerService.this.getCurrentUserStateLocked();
                } else {
                    return false;
                }
            }
        }

        public void setSoftKeyboardCallbackEnabled(boolean enabled) {
            this.mInvocationHandler.setSoftKeyboardCallbackEnabled(enabled);
        }

        public boolean isAccessibilityButtonAvailable() {
            synchronized (AccessibilityManagerService.this.mLock) {
                if (isCalledForCurrentUserLocked()) {
                    boolean isAccessibilityButtonAvailableLocked = isAccessibilityButtonAvailableLocked(AccessibilityManagerService.this.getCurrentUserStateLocked());
                    return isAccessibilityButtonAvailableLocked;
                }
                return false;
            }
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpPermission(AccessibilityManagerService.this.mContext, AccessibilityManagerService.LOG_TAG, pw)) {
                synchronized (AccessibilityManagerService.this.mLock) {
                    if (this.mAccessibilityServiceInfo.getResolveInfo() != null) {
                        pw.append("Service[label=" + this.mAccessibilityServiceInfo.getResolveInfo().loadLabel(AccessibilityManagerService.this.mContext.getPackageManager()));
                    } else {
                        Slog.w(AccessibilityManagerService.LOG_TAG, "dump() mAccessibilityServiceInfo.getResolveInfo() is null");
                        pw.append("Service[label=null");
                    }
                    pw.append(", feedbackType" + AccessibilityServiceInfo.feedbackTypeToString(this.mFeedbackType));
                    pw.append(", capabilities=" + this.mAccessibilityServiceInfo.getCapabilities());
                    pw.append(", eventTypes=" + AccessibilityEvent.eventTypeToString(this.mEventTypes));
                    pw.append(", notificationTimeout=" + this.mNotificationTimeout);
                    pw.append("]");
                }
            }
        }

        public void onServiceDisconnected(ComponentName componentName) {
            binderDied();
        }

        public void onAdded() throws RemoteException {
            long identity = Binder.clearCallingIdentity();
            try {
                AccessibilityManagerService.this.mWindowManagerService.addWindowToken(this.mOverlayWindowToken, 2032, 0);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void onRemoved() {
            long identity = Binder.clearCallingIdentity();
            try {
                AccessibilityManagerService.this.mWindowManagerService.removeWindowToken(this.mOverlayWindowToken, true, 0);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void resetLocked() {
            try {
                if (this.mServiceInterface != null) {
                    this.mServiceInterface.init(null, this.mId, null);
                }
            } catch (RemoteException e) {
            }
            if (this.mService != null) {
                try {
                    this.mService.unlinkToDeath(this, 0);
                } catch (NoSuchElementException e2) {
                    Slog.e(AccessibilityManagerService.LOG_TAG, "resetLocked Unable to unlinkToDeath", e2);
                }
                this.mService = null;
            }
            this.mServiceInterface = null;
            this.mReceivedAccessibilityButtonCallbackSinceBind = false;
        }

        public boolean isConnectedLocked() {
            return this.mService != null;
        }

        public void binderDied() {
            synchronized (AccessibilityManagerService.this.mLock) {
                if (isConnectedLocked()) {
                    this.mWasConnectedAndDied = true;
                    AccessibilityManagerService.this.getKeyEventDispatcher().flush(this);
                    UserState userState = AccessibilityManagerService.this.getUserStateLocked(this.mUserId);
                    resetLocked();
                    if (this.mIsAutomation) {
                        AccessibilityManagerService.this.removeServiceLocked(this, userState);
                        userState.mInstalledServices.remove(this.mAccessibilityServiceInfo);
                        userState.mEnabledServices.remove(this.mComponentName);
                        userState.destroyUiAutomationService();
                        AccessibilityManagerService.this.readConfigurationForUserStateLocked(userState);
                    }
                    if (this.mId == AccessibilityManagerService.this.getMagnificationController().getIdOfLastServiceToMagnify()) {
                        AccessibilityManagerService.this.getMagnificationController().resetIfNeeded(true);
                    }
                    AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                    Slog.i(AccessibilityManagerService.LOG_TAG, "binder Died, set Filter to null.");
                    AccessibilityManagerService.this.mWindowManagerService.setInputFilter(null);
                    return;
                }
            }
        }

        public void notifyAccessibilityEvent(AccessibilityEvent event, boolean serviceWantsEvent) {
            synchronized (AccessibilityManagerService.this.mLock) {
                Message message;
                int eventType = event.getEventType();
                AccessibilityEvent newEvent = AccessibilityEvent.obtain(event);
                if (this.mNotificationTimeout <= 0 || eventType == 2048) {
                    message = this.mEventDispatchHandler.obtainMessage(eventType, newEvent);
                } else {
                    AccessibilityEvent oldEvent = (AccessibilityEvent) this.mPendingEvents.get(eventType);
                    this.mPendingEvents.put(eventType, newEvent);
                    if (oldEvent != null) {
                        this.mEventDispatchHandler.removeMessages(eventType);
                        oldEvent.recycle();
                    }
                    message = this.mEventDispatchHandler.obtainMessage(eventType);
                }
                message.arg1 = serviceWantsEvent ? 1 : 0;
                this.mEventDispatchHandler.sendMessageDelayed(message, this.mNotificationTimeout);
            }
        }

        private boolean isAccessibilityButtonAvailableLocked(UserState userState) {
            if (!this.mRequestAccessibilityButton || !AccessibilityManagerService.this.mIsAccessibilityButtonShown) {
                return false;
            }
            if (userState.mIsNavBarMagnificationEnabled && userState.mIsNavBarMagnificationAssignedToAccessibilityButton) {
                return false;
            }
            int requestingServices = 0;
            for (int i = userState.mBoundServices.size() - 1; i >= 0; i--) {
                if (((Service) userState.mBoundServices.get(i)).mRequestAccessibilityButton) {
                    requestingServices++;
                }
            }
            if (requestingServices == 1 || userState.mServiceAssignedToAccessibilityButton == null) {
                return true;
            }
            return this.mComponentName.equals(userState.mServiceAssignedToAccessibilityButton);
        }

        /* JADX WARNING: Missing block: B:21:?, code:
            r1.onAccessibilityEvent(android.view.accessibility.AccessibilityEvent.obtain(r8), r9);
     */
        /* JADX WARNING: Missing block: B:22:0x0042, code:
            return;
     */
        /* JADX WARNING: Missing block: B:29:0x004d, code:
            r2 = move-exception;
     */
        /* JADX WARNING: Missing block: B:31:?, code:
            android.util.Slog.e(com.android.server.accessibility.AccessibilityManagerService.LOG_TAG, "Error during sending " + r8 + " to " + r1, r2);
     */
        /* JADX WARNING: Missing block: B:33:0x0078, code:
            r8.recycle();
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void notifyAccessibilityEventInternal(int eventType, AccessibilityEvent event, boolean serviceWantsEvent) {
            synchronized (AccessibilityManagerService.this.mLock) {
                IAccessibilityServiceClient listener = this.mServiceInterface;
                if (listener == null) {
                    return;
                }
                if (event == null) {
                    event = (AccessibilityEvent) this.mPendingEvents.get(eventType);
                    if (event == null) {
                        return;
                    }
                    this.mPendingEvents.remove(eventType);
                }
                if (AccessibilityManagerService.this.mSecurityPolicy.canRetrieveWindowContentLocked(this)) {
                    event.setConnectionId(this.mId);
                } else {
                    event.setSource((View) null);
                }
                event.setSealed(true);
            }
        }

        public void notifyGesture(int gestureId) {
            this.mInvocationHandler.obtainMessage(1, gestureId, 0).sendToTarget();
        }

        public void notifyClearAccessibilityNodeInfoCache() {
            this.mInvocationHandler.sendEmptyMessage(2);
        }

        public void notifyMagnificationChangedLocked(Region region, float scale, float centerX, float centerY) {
            this.mInvocationHandler.notifyMagnificationChangedLocked(region, scale, centerX, centerY);
        }

        public void notifySoftKeyboardShowModeChangedLocked(int showState) {
            this.mInvocationHandler.notifySoftKeyboardShowModeChangedLocked(showState);
        }

        public void notifyAccessibilityButtonClickedLocked() {
            this.mInvocationHandler.notifyAccessibilityButtonClickedLocked();
        }

        public void notifyAccessibilityButtonAvailabilityChangedLocked(boolean available) {
            this.mInvocationHandler.notifyAccessibilityButtonAvailabilityChangedLocked(available);
        }

        private void notifyMagnificationChangedInternal(Region region, float scale, float centerX, float centerY) {
            IAccessibilityServiceClient listener;
            synchronized (AccessibilityManagerService.this.mLock) {
                listener = this.mServiceInterface;
            }
            if (listener != null) {
                try {
                    listener.onMagnificationChanged(region, scale, centerX, centerY);
                } catch (RemoteException re) {
                    Slog.e(AccessibilityManagerService.LOG_TAG, "Error sending magnification changes to " + this.mService, re);
                }
            }
        }

        private void notifySoftKeyboardShowModeChangedInternal(int showState) {
            IAccessibilityServiceClient listener;
            synchronized (AccessibilityManagerService.this.mLock) {
                listener = this.mServiceInterface;
            }
            if (listener != null) {
                try {
                    listener.onSoftKeyboardShowModeChanged(showState);
                } catch (RemoteException re) {
                    Slog.e(AccessibilityManagerService.LOG_TAG, "Error sending soft keyboard show mode changes to " + this.mService, re);
                }
            }
        }

        private void notifyAccessibilityButtonClickedInternal() {
            IAccessibilityServiceClient listener;
            synchronized (AccessibilityManagerService.this.mLock) {
                listener = this.mServiceInterface;
            }
            if (listener != null) {
                try {
                    listener.onAccessibilityButtonClicked();
                } catch (RemoteException re) {
                    Slog.e(AccessibilityManagerService.LOG_TAG, "Error sending accessibility button click to " + this.mService, re);
                }
            }
        }

        private void notifyAccessibilityButtonAvailabilityChangedInternal(boolean available) {
            if (!this.mReceivedAccessibilityButtonCallbackSinceBind || this.mLastAccessibilityButtonCallbackState != available) {
                IAccessibilityServiceClient listener;
                this.mReceivedAccessibilityButtonCallbackSinceBind = true;
                this.mLastAccessibilityButtonCallbackState = available;
                synchronized (AccessibilityManagerService.this.mLock) {
                    listener = this.mServiceInterface;
                }
                if (listener != null) {
                    try {
                        listener.onAccessibilityButtonAvailabilityChanged(available);
                    } catch (RemoteException re) {
                        Slog.e(AccessibilityManagerService.LOG_TAG, "Error sending accessibility button availability change to " + this.mService, re);
                    }
                }
            }
        }

        private void notifyGestureInternal(int gestureId) {
            IAccessibilityServiceClient listener;
            synchronized (AccessibilityManagerService.this.mLock) {
                listener = this.mServiceInterface;
            }
            if (listener != null) {
                try {
                    listener.onGesture(gestureId);
                } catch (RemoteException re) {
                    Slog.e(AccessibilityManagerService.LOG_TAG, "Error during sending gesture " + gestureId + " to " + this.mService, re);
                }
            }
        }

        private void notifyClearAccessibilityCacheInternal() {
            IAccessibilityServiceClient listener;
            synchronized (AccessibilityManagerService.this.mLock) {
                listener = this.mServiceInterface;
            }
            if (listener != null) {
                try {
                    listener.clearAccessibilityCache();
                } catch (RemoteException re) {
                    Slog.e(AccessibilityManagerService.LOG_TAG, "Error during requesting accessibility info cache to be cleared.", re);
                }
            }
        }

        private void sendDownAndUpKeyEvents(int keyCode) {
            long token = Binder.clearCallingIdentity();
            long downTime = SystemClock.uptimeMillis();
            KeyEvent down = KeyEvent.obtain(downTime, downTime, 0, keyCode, 0, 0, -1, 0, 8, LightsManager.LIGHT_ID_SMARTBACKLIGHT, null);
            InputManager.getInstance().injectInputEvent(down, 0);
            down.recycle();
            InputEvent up = KeyEvent.obtain(downTime, SystemClock.uptimeMillis(), 1, keyCode, 0, 0, -1, 0, 8, LightsManager.LIGHT_ID_SMARTBACKLIGHT, null);
            InputManager.getInstance().injectInputEvent(up, 0);
            up.recycle();
            Binder.restoreCallingIdentity(token);
        }

        private void expandNotifications() {
            long token = Binder.clearCallingIdentity();
            ((StatusBarManager) AccessibilityManagerService.this.mContext.getSystemService("statusbar")).expandNotificationsPanel();
            Binder.restoreCallingIdentity(token);
        }

        private void expandQuickSettings() {
            long token = Binder.clearCallingIdentity();
            ((StatusBarManager) AccessibilityManagerService.this.mContext.getSystemService("statusbar")).expandSettingsPanel();
            Binder.restoreCallingIdentity(token);
        }

        private boolean openRecents() {
            long token = Binder.clearCallingIdentity();
            try {
                StatusBarManagerInternal statusBarService = (StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class);
                if (statusBarService == null) {
                    return false;
                }
                statusBarService.toggleRecentApps();
                Binder.restoreCallingIdentity(token);
                return true;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        private void showGlobalActions() {
            AccessibilityManagerService.this.mWindowManagerService.showGlobalActions();
        }

        private void toggleSplitScreen() {
            ((StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class)).toggleSplitScreen();
        }

        private IAccessibilityInteractionConnection getConnectionLocked(int windowId) {
            AccessibilityConnectionWrapper wrapper = (AccessibilityConnectionWrapper) AccessibilityManagerService.this.mGlobalInteractionConnections.get(windowId);
            if (wrapper == null) {
                wrapper = (AccessibilityConnectionWrapper) AccessibilityManagerService.this.getCurrentUserStateLocked().mInteractionConnections.get(windowId);
            }
            if (wrapper == null || wrapper.mConnection == null) {
                return null;
            }
            return wrapper.mConnection;
        }

        private int resolveAccessibilityWindowIdLocked(int accessibilityWindowId) {
            if (accessibilityWindowId == HwBootFail.STAGE_BOOT_SUCCESS) {
                return AccessibilityManagerService.this.mSecurityPolicy.getActiveWindowId();
            }
            return accessibilityWindowId;
        }

        private int resolveAccessibilityWindowIdForFindFocusLocked(int windowId, int focusType) {
            if (windowId == HwBootFail.STAGE_BOOT_SUCCESS) {
                return AccessibilityManagerService.this.mSecurityPolicy.mActiveWindowId;
            }
            if (windowId == -2) {
                if (focusType == 1) {
                    return AccessibilityManagerService.this.mSecurityPolicy.mFocusedWindowId;
                }
                if (focusType == 2) {
                    return AccessibilityManagerService.this.mSecurityPolicy.mAccessibilityFocusedWindowId;
                }
            }
            return windowId;
        }

        private IAccessibilityInteractionConnectionCallback replaceCallbackIfNeeded(IAccessibilityInteractionConnectionCallback originalCallback, int resolvedWindowId, int interactionId, int interrogatingPid, long interrogatingTid) {
            AccessibilityWindowInfo windowInfo = AccessibilityManagerService.this.mSecurityPolicy.findWindowById(resolvedWindowId);
            if (windowInfo == null || (windowInfo.inPictureInPicture() ^ 1) != 0 || AccessibilityManagerService.this.mPictureInPictureActionReplacingConnection == null) {
                return originalCallback;
            }
            return new ActionReplacingCallback(originalCallback, AccessibilityManagerService.this.mPictureInPictureActionReplacingConnection.mConnection, interactionId, interrogatingPid, interrogatingTid);
        }
    }

    private class UserState {
        public boolean mAccessibilityFocusOnlyInActiveWindow;
        public final Set<ComponentName> mBindingServices = new HashSet();
        public final CopyOnWriteArrayList<Service> mBoundServices = new CopyOnWriteArrayList();
        public final Map<ComponentName, Service> mComponentNameToServiceMap = new HashMap();
        public final Set<ComponentName> mEnabledServices = new HashSet();
        public final List<AccessibilityServiceInfo> mInstalledServices = new ArrayList();
        public final SparseArray<AccessibilityConnectionWrapper> mInteractionConnections = new SparseArray();
        public boolean mIsAutoclickEnabled;
        public boolean mIsDisplayMagnificationEnabled;
        public boolean mIsFilterKeyEventsEnabled;
        public boolean mIsNavBarMagnificationAssignedToAccessibilityButton;
        public boolean mIsNavBarMagnificationEnabled;
        public boolean mIsPerformGesturesEnabled;
        public boolean mIsTextHighContrastEnabled;
        public boolean mIsTouchExplorationEnabled;
        public int mLastSentClientState = -1;
        public int mLastSentRelevantEventTypes = -1;
        public ComponentName mServiceAssignedToAccessibilityButton;
        public ComponentName mServiceChangingSoftKeyboardMode;
        public ComponentName mServiceToEnableWithShortcut;
        public int mSoftKeyboardShowMode = 0;
        public final Set<ComponentName> mTouchExplorationGrantedServices = new HashSet();
        private int mUiAutomationFlags;
        private final DeathRecipient mUiAutomationSerivceOnwerDeathRecipient = new DeathRecipient() {
            public void binderDied() {
                UserState.this.mUiAutomationServiceOwner.unlinkToDeath(UserState.this.mUiAutomationSerivceOnwerDeathRecipient, 0);
                UserState.this.mUiAutomationServiceOwner = null;
                if (UserState.this.mUiAutomationService != null) {
                    UserState.this.mUiAutomationService.binderDied();
                }
            }
        };
        private Service mUiAutomationService;
        private IAccessibilityServiceClient mUiAutomationServiceClient;
        private IBinder mUiAutomationServiceOwner;
        public final RemoteCallbackList<IAccessibilityManagerClient> mUserClients = new RemoteCallbackList();
        public final int mUserId;
        public final SparseArray<IBinder> mWindowTokens = new SparseArray();

        public UserState(int userId) {
            this.mUserId = userId;
        }

        public int getClientState() {
            int clientState = 0;
            if (isHandlingAccessibilityEvents()) {
                clientState = 1;
            }
            if (isHandlingAccessibilityEvents() && this.mIsTouchExplorationEnabled) {
                clientState |= 2;
            }
            if (this.mIsTextHighContrastEnabled) {
                return clientState | 4;
            }
            return clientState;
        }

        public boolean isHandlingAccessibilityEvents() {
            return this.mBoundServices.isEmpty() ? this.mBindingServices.isEmpty() ^ 1 : true;
        }

        public void onSwitchToAnotherUser() {
            if (this.mUiAutomationService != null) {
                this.mUiAutomationService.binderDied();
            }
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

        public void destroyUiAutomationService() {
            this.mUiAutomationService = null;
            this.mUiAutomationFlags = 0;
            this.mUiAutomationServiceClient = null;
            if (this.mUiAutomationServiceOwner != null) {
                this.mUiAutomationServiceOwner.unlinkToDeath(this.mUiAutomationSerivceOnwerDeathRecipient, 0);
                this.mUiAutomationServiceOwner = null;
            }
        }

        boolean isUiAutomationSuppressingOtherServices() {
            return this.mUiAutomationService != null && (this.mUiAutomationFlags & 1) == 0;
        }
    }

    final class WindowsForAccessibilityCallback implements android.view.WindowManagerInternal.WindowsForAccessibilityCallback {
        WindowsForAccessibilityCallback() {
        }

        public void onWindowsForAccessibilityChanged(List<WindowInfo> windows) {
            synchronized (AccessibilityManagerService.this.mLock) {
                List<AccessibilityWindowInfo> reportedWindows = new ArrayList();
                int receivedWindowCount = windows.size();
                for (int i = 0; i < receivedWindowCount; i++) {
                    AccessibilityWindowInfo reportedWindow = populateReportedWindow((WindowInfo) windows.get(i));
                    if (reportedWindow != null) {
                        reportedWindows.add(reportedWindow);
                    }
                }
                AccessibilityManagerService.this.mSecurityPolicy.updateWindowsLocked(reportedWindows);
                AccessibilityManagerService.this.mLock.notifyAll();
            }
        }

        private AccessibilityWindowInfo populateReportedWindow(WindowInfo window) {
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
            switch (windowType) {
                case 1:
                case 2:
                case 3:
                case 4:
                case 1000:
                case NetworkAgentInfo.EVENT_NETWORK_LINGER_COMPLETE /*1001*/:
                case 1002:
                case 1003:
                case 1005:
                case 2002:
                case 2005:
                case 2007:
                    return 1;
                case IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME /*2000*/:
                case 2001:
                case 2003:
                case 2006:
                case 2008:
                case 2009:
                case 2010:
                case 2014:
                case 2017:
                case 2019:
                case 2020:
                case 2024:
                case 2036:
                case 2038:
                    return 3;
                case 2011:
                case 2012:
                    return 2;
                case 2032:
                    return 4;
                case 2034:
                    return 5;
                default:
                    return -1;
            }
        }
    }

    private UserState getCurrentUserStateLocked() {
        return getUserStateLocked(this.mCurrentUserId);
    }

    public AccessibilityManagerService(Context context) {
        this.mContext = context;
        this.mPackageManager = this.mContext.getPackageManager();
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mWindowManagerService = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
        this.mUserManager = (UserManager) context.getSystemService("user");
        this.mSecurityPolicy = new SecurityPolicy();
        this.mMainHandler = new MainHandler(this.mContext.getMainLooper());
        registerBroadcastReceivers();
        new AccessibilityContentObserver(this.mMainHandler).register(context.getContentResolver());
    }

    private UserState getUserStateLocked(int userId) {
        UserState state = (UserState) this.mUserStates.get(userId);
        if (state != null) {
            return state;
        }
        state = new UserState(userId);
        this.mUserStates.put(userId, state);
        return state;
    }

    private void registerBroadcastReceivers() {
        new PackageMonitor() {
            /* JADX WARNING: Missing block: B:14:0x0034, code:
            return;
     */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onSomePackagesChanged() {
                synchronized (AccessibilityManagerService.this.mLock) {
                    if (getChangingUserId() != AccessibilityManagerService.this.mCurrentUserId) {
                        return;
                    }
                    UserState userState = AccessibilityManagerService.this.getCurrentUserStateLocked();
                    userState.mInstalledServices.clear();
                    if (!userState.isUiAutomationSuppressingOtherServices() && AccessibilityManagerService.this.readConfigurationForUserStateLocked(userState)) {
                        AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                    }
                }
            }

            /* JADX WARNING: Missing block: B:17:0x0049, code:
            return;
     */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onPackageUpdateFinished(String packageName, int uid) {
                synchronized (AccessibilityManagerService.this.mLock) {
                    int userId = getChangingUserId();
                    if (userId != AccessibilityManagerService.this.mCurrentUserId) {
                        return;
                    }
                    UserState userState = AccessibilityManagerService.this.getUserStateLocked(userId);
                    boolean unboundAService = false;
                    for (int i = userState.mBoundServices.size() - 1; i >= 0; i--) {
                        Service boundService = (Service) userState.mBoundServices.get(i);
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

            /* JADX WARNING: Missing block: B:17:0x005f, code:
            return;
     */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onPackageRemoved(String packageName, int uid) {
                synchronized (AccessibilityManagerService.this.mLock) {
                    int userId = getChangingUserId();
                    if (userId != AccessibilityManagerService.this.mCurrentUserId) {
                        return;
                    }
                    UserState userState = AccessibilityManagerService.this.getUserStateLocked(userId);
                    Iterator<ComponentName> it = userState.mEnabledServices.iterator();
                    while (it.hasNext()) {
                        ComponentName comp = (ComponentName) it.next();
                        if (comp.getPackageName().equals(packageName)) {
                            it.remove();
                            AccessibilityManagerService.this.persistComponentNamesToSettingLocked("enabled_accessibility_services", userState.mEnabledServices, userId);
                            userState.mTouchExplorationGrantedServices.remove(comp);
                            AccessibilityManagerService.this.persistComponentNamesToSettingLocked("touch_exploration_granted_accessibility_services", userState.mTouchExplorationGrantedServices, userId);
                            if (!userState.isUiAutomationSuppressingOtherServices()) {
                                AccessibilityManagerService.this.onUserStateChangedLocked(userState);
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
                        String compPkg = ((ComponentName) it.next()).getPackageName();
                        for (String pkg : packages) {
                            if (compPkg.equals(pkg)) {
                                if (!doit) {
                                    return true;
                                } else if (HwDeviceManager.disallowOp(53, pkg)) {
                                    Slog.w(AccessibilityManagerService.LOG_TAG, " onHandleForceStop. this forcestop pkg =" + compPkg + " in MDM accessibility whitelist ,block!");
                                } else {
                                    it.remove();
                                    AccessibilityManagerService.this.persistComponentNamesToSettingLocked("enabled_accessibility_services", userState.mEnabledServices, userId);
                                    if (!userState.isUiAutomationSuppressingOtherServices()) {
                                        AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                                    }
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
                    return;
                }
                UserState userState;
                Object -get9;
                if ("android.intent.action.LOCKED_BOOT_COMPLETED".equals(action)) {
                    userState = AccessibilityManagerService.this.getCurrentUserStateLocked();
                    -get9 = AccessibilityManagerService.this.mLock;
                    synchronized (-get9) {
                        AccessibilityManagerService.this.readConfigurationForUserStateLocked(userState);
                        AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                    }
                } else if ("android.intent.action.USER_UNLOCKED".equals(action)) {
                    AccessibilityManagerService.this.unlockUser(intent.getIntExtra("android.intent.extra.user_handle", 0));
                    return;
                } else if ("android.intent.action.USER_REMOVED".equals(action)) {
                    AccessibilityManagerService.this.removeUser(intent.getIntExtra("android.intent.extra.user_handle", 0));
                    return;
                } else if ("android.intent.action.USER_PRESENT".equals(action)) {
                    -get9 = AccessibilityManagerService.this.mLock;
                    synchronized (-get9) {
                        userState = AccessibilityManagerService.this.getCurrentUserStateLocked();
                        if (!userState.isUiAutomationSuppressingOtherServices() && AccessibilityManagerService.this.readConfigurationForUserStateLocked(userState)) {
                            AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                        }
                    }
                } else if ("android.os.action.SETTING_RESTORED".equals(action)) {
                    if ("enabled_accessibility_services".equals(intent.getStringExtra("setting_name"))) {
                        -get9 = AccessibilityManagerService.this.mLock;
                        synchronized (-get9) {
                            AccessibilityManagerService.this.restoreEnabledAccessibilityServicesLocked(intent.getStringExtra("previous_value"), intent.getStringExtra("new_value"));
                        }
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            }
        }, UserHandle.ALL, intentFilter, null, null);
    }

    public long addClient(IAccessibilityManagerClient client, int userId) {
        synchronized (this.mLock) {
            int resolvedUserId = this.mSecurityPolicy.resolveCallingUserIdEnforcingPermissionsLocked(userId);
            UserState userState = getUserStateLocked(resolvedUserId);
            long of;
            if (this.mSecurityPolicy.isCallerInteractingAcrossUsers(userId)) {
                this.mGlobalClients.register(client);
                of = IntPair.of(userState.getClientState(), userState.mLastSentRelevantEventTypes);
                return of;
            }
            userState.mUserClients.register(client);
            of = IntPair.of(resolvedUserId == this.mCurrentUserId ? userState.getClientState() : 0, userState.mLastSentRelevantEventTypes);
            return of;
        }
    }

    public void sendAccessibilityEvent(AccessibilityEvent event, int userId) {
        boolean dispatchEvent = false;
        synchronized (this.mLock) {
            if (event.getWindowId() == -3) {
                AccessibilityWindowInfo pip = this.mSecurityPolicy.getPictureInPictureWindow();
                if (pip != null) {
                    int pipId = pip.getId();
                    event.setWindowId(pipId);
                    event.setSealed(true);
                    AccessibilityNodeInfo info = event.getSource();
                    info.setSealed(false);
                    event.setSealed(false);
                    if (info != null) {
                        info.setSourceNodeId(info.getSourceNodeId(), pipId);
                        event.setSource(info);
                        info.recycle();
                    }
                }
            }
            if (this.mSecurityPolicy.resolveCallingUserIdEnforcingPermissionsLocked(userId) == this.mCurrentUserId) {
                if (this.mSecurityPolicy.canDispatchAccessibilityEventLocked(event)) {
                    this.mSecurityPolicy.updateActiveAndAccessibilityFocusedWindowLocked(event.getWindowId(), event.getSourceNodeId(), event.getEventType(), event.getAction());
                    this.mSecurityPolicy.updateEventSourceLocked(event);
                    dispatchEvent = true;
                }
                if (this.mHasInputFilter && this.mInputFilter != null) {
                    this.mMainHandler.obtainMessage(1, AccessibilityEvent.obtain(event)).sendToTarget();
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
            }
        }
        if (OWN_PROCESS_ID != Binder.getCallingPid()) {
            event.recycle();
        }
    }

    public List<AccessibilityServiceInfo> getInstalledAccessibilityServiceList(int userId) {
        synchronized (this.mLock) {
            UserState userState = getUserStateLocked(this.mSecurityPolicy.resolveCallingUserIdEnforcingPermissionsLocked(userId));
            if (userState.mUiAutomationService != null) {
                List<AccessibilityServiceInfo> installedServices = new ArrayList();
                installedServices.addAll(userState.mInstalledServices);
                installedServices.remove(userState.mUiAutomationService.mAccessibilityServiceInfo);
                return installedServices;
            }
            List<AccessibilityServiceInfo> list = userState.mInstalledServices;
            return list;
        }
    }

    public List<AccessibilityServiceInfo> getEnabledAccessibilityServiceList(int feedbackType, int userId) {
        synchronized (this.mLock) {
            UserState userState = getUserStateLocked(this.mSecurityPolicy.resolveCallingUserIdEnforcingPermissionsLocked(userId));
            if (userState.isUiAutomationSuppressingOtherServices()) {
                List<AccessibilityServiceInfo> emptyList = Collections.emptyList();
                return emptyList;
            }
            List<Service> services = userState.mBoundServices;
            int serviceCount = services.size();
            List<AccessibilityServiceInfo> result = new ArrayList(serviceCount);
            for (int i = 0; i < serviceCount; i++) {
                Service service = (Service) services.get(i);
                if (!(sFakeAccessibilityServiceComponentName.equals(service.mComponentName) || (service.mFeedbackType & feedbackType) == 0)) {
                    result.add(service.mAccessibilityServiceInfo);
                }
            }
            return result;
        }
    }

    /* JADX WARNING: Missing block: B:16:0x0036, code:
            r3 = 0;
            r2 = r4.size();
     */
    /* JADX WARNING: Missing block: B:17:0x003b, code:
            if (r3 >= r2) goto L_0x006c;
     */
    /* JADX WARNING: Missing block: B:19:?, code:
            ((android.accessibilityservice.IAccessibilityServiceClient) r4.get(r3)).onInterrupt();
     */
    /* JADX WARNING: Missing block: B:24:0x004c, code:
            r6 = move-exception;
     */
    /* JADX WARNING: Missing block: B:25:0x004d, code:
            android.util.Slog.e(LOG_TAG, "Error sending interrupt request to " + r4.get(r3), r6);
     */
    /* JADX WARNING: Missing block: B:26:0x006c, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void interrupt(int userId) {
        int i;
        synchronized (this.mLock) {
            int resolvedUserId = this.mSecurityPolicy.resolveCallingUserIdEnforcingPermissionsLocked(userId);
            if (resolvedUserId != this.mCurrentUserId) {
                return;
            }
            List<Service> services = getUserStateLocked(resolvedUserId).mBoundServices;
            int numServices = services.size();
            List<IAccessibilityServiceClient> interfacesToInterrupt = new ArrayList(numServices);
            for (i = 0; i < numServices; i++) {
                Service service = (Service) services.get(i);
                IBinder a11yServiceBinder = service.mService;
                IAccessibilityServiceClient a11yServiceInterface = service.mServiceInterface;
                if (!(a11yServiceBinder == null || a11yServiceInterface == null)) {
                    interfacesToInterrupt.add(a11yServiceInterface);
                }
            }
        }
        i++;
    }

    public int addAccessibilityInteractionConnection(IWindow windowToken, IAccessibilityInteractionConnection connection, int userId) throws RemoteException {
        int windowId;
        synchronized (this.mLock) {
            int resolvedUserId = this.mSecurityPolicy.resolveCallingUserIdEnforcingPermissionsLocked(userId);
            windowId = sNextWindowId;
            sNextWindowId = windowId + 1;
            AccessibilityConnectionWrapper wrapper;
            if (this.mSecurityPolicy.isCallerInteractingAcrossUsers(userId)) {
                wrapper = new AccessibilityConnectionWrapper(windowId, connection, -1);
                wrapper.linkToDeath();
                this.mGlobalInteractionConnections.put(windowId, wrapper);
                this.mGlobalWindowTokens.put(windowId, windowToken.asBinder());
            } else {
                wrapper = new AccessibilityConnectionWrapper(windowId, connection, resolvedUserId);
                wrapper.linkToDeath();
                UserState userState = getUserStateLocked(resolvedUserId);
                userState.mInteractionConnections.put(windowId, wrapper);
                userState.mWindowTokens.put(windowId, windowToken.asBinder());
            }
        }
        return windowId;
    }

    public void removeAccessibilityInteractionConnection(IWindow window) {
        synchronized (this.mLock) {
            this.mSecurityPolicy.resolveCallingUserIdEnforcingPermissionsLocked(UserHandle.getCallingUserId());
            IBinder token = window.asBinder();
            if (removeAccessibilityInteractionConnectionInternalLocked(token, this.mGlobalWindowTokens, this.mGlobalInteractionConnections) >= 0) {
                return;
            }
            int userCount = this.mUserStates.size();
            for (int i = 0; i < userCount; i++) {
                UserState userState = (UserState) this.mUserStates.valueAt(i);
                if (removeAccessibilityInteractionConnectionInternalLocked(token, userState.mWindowTokens, userState.mInteractionConnections) >= 0) {
                    return;
                }
            }
        }
    }

    private int removeAccessibilityInteractionConnectionInternalLocked(IBinder windowToken, SparseArray<IBinder> windowTokens, SparseArray<AccessibilityConnectionWrapper> interactionConnections) {
        int count = windowTokens.size();
        for (int i = 0; i < count; i++) {
            if (windowTokens.valueAt(i) == windowToken) {
                int windowId = windowTokens.keyAt(i);
                windowTokens.removeAt(i);
                ((AccessibilityConnectionWrapper) interactionConnections.get(windowId)).unlinkToDeath();
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
                AccessibilityConnectionWrapper wrapper = new AccessibilityConnectionWrapper(-3, connection, -1);
                this.mPictureInPictureActionReplacingConnection = wrapper;
                wrapper.linkToDeath();
            }
            this.mSecurityPolicy.notifyWindowsChanged();
        }
    }

    public void registerUiTestAutomationService(IBinder owner, IAccessibilityServiceClient serviceClient, AccessibilityServiceInfo accessibilityServiceInfo, int flags) {
        this.mSecurityPolicy.enforceCallingPermission("android.permission.RETRIEVE_WINDOW_CONTENT", FUNCTION_REGISTER_UI_TEST_AUTOMATION_SERVICE);
        accessibilityServiceInfo.setComponentName(sFakeAccessibilityServiceComponentName);
        synchronized (this.mLock) {
            UserState userState = getCurrentUserStateLocked();
            if (userState.mUiAutomationService != null) {
                throw new IllegalStateException("UiAutomationService " + serviceClient + "already registered!");
            }
            try {
                owner.linkToDeath(userState.mUiAutomationSerivceOnwerDeathRecipient, 0);
                userState.mUiAutomationServiceOwner = owner;
                userState.mUiAutomationServiceClient = serviceClient;
                userState.mUiAutomationFlags = flags;
                userState.mInstalledServices.add(accessibilityServiceInfo);
                if ((flags & 1) == 0) {
                    userState.mIsTouchExplorationEnabled = false;
                    userState.mIsDisplayMagnificationEnabled = false;
                    userState.mIsNavBarMagnificationEnabled = false;
                    userState.mIsAutoclickEnabled = false;
                    userState.mEnabledServices.clear();
                }
                userState.mEnabledServices.add(sFakeAccessibilityServiceComponentName);
                userState.mTouchExplorationGrantedServices.add(sFakeAccessibilityServiceComponentName);
                onUserStateChangedLocked(userState);
            } catch (RemoteException re) {
                Slog.e(LOG_TAG, "Couldn't register for the death of a UiTestAutomationService!", re);
            }
        }
    }

    public void unregisterUiTestAutomationService(IAccessibilityServiceClient serviceClient) {
        synchronized (this.mLock) {
            UserState userState = getCurrentUserStateLocked();
            if (userState.mUiAutomationService == null || serviceClient == null || userState.mUiAutomationService.mServiceInterface == null || userState.mUiAutomationService.mServiceInterface.asBinder() != serviceClient.asBinder()) {
                throw new IllegalStateException("UiAutomationService " + serviceClient + " not registered!");
            }
            userState.mUiAutomationService.binderDied();
        }
    }

    public void temporaryEnableAccessibilityStateUntilKeyguardRemoved(ComponentName service, boolean touchExplorationEnabled) {
        this.mSecurityPolicy.enforceCallingPermission("android.permission.TEMPORARY_ENABLE_ACCESSIBILITY", TEMPORARY_ENABLE_ACCESSIBILITY_UNTIL_KEYGUARD_REMOVED);
        if (this.mWindowManagerService.isKeyguardLocked()) {
            synchronized (this.mLock) {
                UserState userState = getCurrentUserStateLocked();
                if (userState.isUiAutomationSuppressingOtherServices()) {
                    return;
                }
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
            } else if (this.mSecurityPolicy.findWindowById(windowId) == null) {
                return null;
            } else {
                IBinder token = (IBinder) this.mGlobalWindowTokens.get(windowId);
                if (token != null) {
                    return token;
                }
                IBinder iBinder = (IBinder) getCurrentUserStateLocked().mWindowTokens.get(windowId);
                return iBinder;
            }
        }
    }

    public void notifyAccessibilityButtonClicked() {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.STATUS_BAR_SERVICE") != 0) {
            throw new SecurityException("Caller does not hold permission android.permission.STATUS_BAR_SERVICE");
        }
        synchronized (this.mLock) {
            notifyAccessibilityButtonClickedLocked();
        }
    }

    public void notifyAccessibilityButtonVisibilityChanged(boolean shown) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.STATUS_BAR_SERVICE") != 0) {
            throw new SecurityException("Caller does not hold permission android.permission.STATUS_BAR_SERVICE");
        }
        synchronized (this.mLock) {
            notifyAccessibilityButtonVisibilityChangedLocked(shown);
        }
    }

    boolean onGesture(int gestureId) {
        boolean handled;
        synchronized (this.mLock) {
            handled = notifyGestureLocked(gestureId, false);
            if (!handled) {
                handled = notifyGestureLocked(gestureId, true);
            }
        }
        return handled;
    }

    boolean notifyKeyEvent(KeyEvent event, int policyFlags) {
        synchronized (this.mLock) {
            List<Service> boundServices = getCurrentUserStateLocked().mBoundServices;
            if (boundServices.isEmpty()) {
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

    void setMotionEventInjector(MotionEventInjector motionEventInjector) {
        synchronized (this.mLock) {
            this.mMotionEventInjector = motionEventInjector;
            this.mLock.notifyAll();
        }
    }

    boolean getAccessibilityFocusClickPointInScreen(Point outPoint) {
        return getInteractionBridge().getAccessibilityFocusClickPointInScreenNotLocked(outPoint);
    }

    public boolean performActionOnAccessibilityFocusedItem(AccessibilityAction action) {
        return getInteractionBridge().performActionOnAccessibilityFocusedItemNotLocked(action);
    }

    boolean getWindowBounds(int windowId, Rect outBounds) {
        IBinder token;
        synchronized (this.mLock) {
            token = (IBinder) this.mGlobalWindowTokens.get(windowId);
            if (token == null) {
                token = (IBinder) getCurrentUserStateLocked().mWindowTokens.get(windowId);
            }
        }
        this.mWindowManagerService.getWindowFrame(token, outBounds);
        if (outBounds.isEmpty()) {
            return false;
        }
        return true;
    }

    boolean accessibilityFocusOnlyInActiveWindow() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mWindowsForAccessibilityCallback == null;
        }
        return z;
    }

    int getActiveWindowId() {
        return this.mSecurityPolicy.getActiveWindowId();
    }

    void onTouchInteractionStart() {
        this.mSecurityPolicy.onTouchInteractionStart();
    }

    void onTouchInteractionEnd() {
        this.mSecurityPolicy.onTouchInteractionEnd();
    }

    /* JADX WARNING: Missing block: B:23:0x0064, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void switchUser(int userId) {
        synchronized (this.mLock) {
            if (this.mCurrentUserId == userId && this.mInitialized) {
                return;
            }
            UserState oldUserState = getCurrentUserStateLocked();
            oldUserState.onSwitchToAnotherUser();
            if (oldUserState.mUserClients.getRegisteredCallbackCount() > 0) {
                this.mMainHandler.obtainMessage(3, oldUserState.mUserId, 0).sendToTarget();
            }
            boolean announceNewUser = ((UserManager) this.mContext.getSystemService("user")).getUsers().size() > 1;
            this.mCurrentUserId = userId;
            UserState userState = getCurrentUserStateLocked();
            if (userState.mUiAutomationService != null) {
                userState.mUiAutomationService.binderDied();
            }
            readConfigurationForUserStateLocked(userState);
            onUserStateChangedLocked(userState);
            if (announceNewUser) {
                this.mMainHandler.sendEmptyMessageDelayed(5, 3000);
            }
        }
    }

    private void unlockUser(int userId) {
        synchronized (this.mLock) {
            if (this.mSecurityPolicy.resolveProfileParentLocked(userId) == this.mCurrentUserId) {
                onUserStateChangedLocked(getUserStateLocked(this.mCurrentUserId));
            }
        }
    }

    private void removeUser(int userId) {
        synchronized (this.mLock) {
            this.mUserStates.remove(userId);
        }
    }

    void restoreEnabledAccessibilityServicesLocked(String oldSetting, String newSetting) {
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
            Service service = (Service) state.mBoundServices.get(i);
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
            ((Service) state.mBoundServices.get(i)).notifyClearAccessibilityNodeInfoCache();
        }
    }

    private void notifyMagnificationChangedLocked(Region region, float scale, float centerX, float centerY) {
        UserState state = getCurrentUserStateLocked();
        for (int i = state.mBoundServices.size() - 1; i >= 0; i--) {
            ((Service) state.mBoundServices.get(i)).notifyMagnificationChangedLocked(region, scale, centerX, centerY);
        }
    }

    private void notifySoftKeyboardShowModeChangedLocked(int showMode) {
        UserState state = getCurrentUserStateLocked();
        for (int i = state.mBoundServices.size() - 1; i >= 0; i--) {
            ((Service) state.mBoundServices.get(i)).notifySoftKeyboardShowModeChangedLocked(showMode);
        }
    }

    private void notifyAccessibilityButtonClickedLocked() {
        int i;
        UserState state = getCurrentUserStateLocked();
        int potentialTargets = state.mIsNavBarMagnificationEnabled ? 1 : 0;
        for (i = state.mBoundServices.size() - 1; i >= 0; i--) {
            if (((Service) state.mBoundServices.get(i)).mRequestAccessibilityButton) {
                potentialTargets++;
            }
        }
        if (potentialTargets != 0) {
            Service service;
            if (potentialTargets != 1) {
                if (state.mServiceAssignedToAccessibilityButton == null && (state.mIsNavBarMagnificationAssignedToAccessibilityButton ^ 1) != 0) {
                    this.mMainHandler.obtainMessage(14).sendToTarget();
                } else if (state.mIsNavBarMagnificationEnabled && state.mIsNavBarMagnificationAssignedToAccessibilityButton) {
                    this.mMainHandler.obtainMessage(13).sendToTarget();
                    return;
                } else {
                    for (i = state.mBoundServices.size() - 1; i >= 0; i--) {
                        service = (Service) state.mBoundServices.get(i);
                        if (service.mRequestAccessibilityButton && service.mComponentName.equals(state.mServiceAssignedToAccessibilityButton)) {
                            service.notifyAccessibilityButtonClickedLocked();
                            return;
                        }
                    }
                }
                this.mMainHandler.obtainMessage(14).sendToTarget();
            } else if (state.mIsNavBarMagnificationEnabled) {
                this.mMainHandler.obtainMessage(13).sendToTarget();
            } else {
                for (i = state.mBoundServices.size() - 1; i >= 0; i--) {
                    service = (Service) state.mBoundServices.get(i);
                    if (service.mRequestAccessibilityButton) {
                        service.notifyAccessibilityButtonClickedLocked();
                        return;
                    }
                }
            }
        }
    }

    private void notifyAccessibilityButtonVisibilityChangedLocked(boolean available) {
        UserState state = getCurrentUserStateLocked();
        this.mIsAccessibilityButtonShown = available;
        for (int i = state.mBoundServices.size() - 1; i >= 0; i--) {
            Service service = (Service) state.mBoundServices.get(i);
            if (service.mRequestAccessibilityButton) {
                service.notifyAccessibilityButtonAvailabilityChangedLocked(service.isAccessibilityButtonAvailableLocked(state));
            }
        }
    }

    private void removeAccessibilityInteractionConnectionLocked(int windowId, int userId) {
        if (userId == -1) {
            this.mGlobalWindowTokens.remove(windowId);
            this.mGlobalInteractionConnections.remove(windowId);
            return;
        }
        UserState userState = getCurrentUserStateLocked();
        userState.mWindowTokens.remove(windowId);
        userState.mInteractionConnections.remove(windowId);
    }

    /* JADX WARNING: Removed duplicated region for block: B:8:0x0077 A:{Splitter: B:6:0x006a, ExcHandler: org.xmlpull.v1.XmlPullParserException (r6_0 'xppe' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:8:0x0077, code:
            r6 = move-exception;
     */
    /* JADX WARNING: Missing block: B:9:0x0078, code:
            android.util.Slog.e(LOG_TAG, "Error while initializing AccessibilityServiceInfo", r6);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean readInstalledAccessibilityServiceLocked(UserState userState) {
        this.mTempAccessibilityServiceInfoList.clear();
        List<ResolveInfo> installedServices = this.mPackageManager.queryIntentServicesAsUser(new Intent("android.accessibilityservice.AccessibilityService"), 819332, this.mCurrentUserId);
        int count = installedServices.size();
        for (int i = 0; i < count; i++) {
            ResolveInfo resolveInfo = (ResolveInfo) installedServices.get(i);
            ServiceInfo serviceInfo = resolveInfo.serviceInfo;
            if ("android.permission.BIND_ACCESSIBILITY_SERVICE".equals(serviceInfo.permission)) {
                try {
                    this.mTempAccessibilityServiceInfoList.add(new AccessibilityServiceInfo(resolveInfo, this.mContext));
                } catch (Exception xppe) {
                }
            } else {
                Slog.w(LOG_TAG, "Skipping accessibilty service " + new ComponentName(serviceInfo.packageName, serviceInfo.name).flattenToShortString() + ": it does not require the permission " + "android.permission.BIND_ACCESSIBILITY_SERVICE");
            }
        }
        if (this.mTempAccessibilityServiceInfoList.equals(userState.mInstalledServices)) {
            this.mTempAccessibilityServiceInfoList.clear();
            return false;
        }
        userState.mInstalledServices.clear();
        userState.mInstalledServices.addAll(this.mTempAccessibilityServiceInfoList);
        this.mTempAccessibilityServiceInfoList.clear();
        return true;
    }

    private boolean readEnabledAccessibilityServicesLocked(UserState userState) {
        this.mTempComponentNameSet.clear();
        readComponentNamesFromSettingLocked("enabled_accessibility_services", userState.mUserId, this.mTempComponentNameSet);
        if (this.mTempComponentNameSet.equals(userState.mEnabledServices)) {
            this.mTempComponentNameSet.clear();
            return false;
        }
        userState.mEnabledServices.clear();
        userState.mEnabledServices.addAll(this.mTempComponentNameSet);
        if (userState.mUiAutomationService != null) {
            userState.mEnabledServices.add(sFakeAccessibilityServiceComponentName);
        }
        this.mTempComponentNameSet.clear();
        return true;
    }

    private boolean readTouchExplorationGrantedAccessibilityServicesLocked(UserState userState) {
        this.mTempComponentNameSet.clear();
        readComponentNamesFromSettingLocked("touch_exploration_granted_accessibility_services", userState.mUserId, this.mTempComponentNameSet);
        if (this.mTempComponentNameSet.equals(userState.mTouchExplorationGrantedServices)) {
            this.mTempComponentNameSet.clear();
            return false;
        }
        userState.mTouchExplorationGrantedServices.clear();
        userState.mTouchExplorationGrantedServices.addAll(this.mTempComponentNameSet);
        this.mTempComponentNameSet.clear();
        return true;
    }

    private void notifyAccessibilityServicesDelayedLocked(AccessibilityEvent event, boolean isDefault) {
        try {
            UserState state = getCurrentUserStateLocked();
            int count = state.mBoundServices.size();
            for (int i = 0; i < count; i++) {
                Service service = (Service) state.mBoundServices.get(i);
                if (service.mIsDefault == isDefault) {
                    if (doesServiceWantEventLocked(service, event)) {
                        service.notifyAccessibilityEvent(event, true);
                    } else if (service.mUsesAccessibilityCache && (event.getEventType() & 4307005) != 0) {
                        service.notifyAccessibilityEvent(event, false);
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
        }
    }

    private void addServiceLocked(Service service, UserState userState) {
        try {
            if (!userState.mBoundServices.contains(service)) {
                service.onAdded();
                userState.mBoundServices.add(service);
                userState.mComponentNameToServiceMap.put(service.mComponentName, service);
                scheduleNotifyClientsOfServicesStateChange(userState);
            }
        } catch (RemoteException e) {
        }
    }

    private void removeServiceLocked(Service service, UserState userState) {
        userState.mBoundServices.remove(service);
        service.onRemoved();
        userState.mComponentNameToServiceMap.clear();
        for (int i = 0; i < userState.mBoundServices.size(); i++) {
            Service boundService = (Service) userState.mBoundServices.get(i);
            userState.mComponentNameToServiceMap.put(boundService.mComponentName, boundService);
        }
        scheduleNotifyClientsOfServicesStateChange(userState);
    }

    private void updateRelevantEventsLocked(UserState userState) {
        int relevantEventTypes = 4307005;
        for (Service service : userState.mBoundServices) {
            relevantEventTypes |= service.mEventTypes;
        }
        int finalRelevantEventTypes = relevantEventTypes;
        if (userState.mLastSentRelevantEventTypes != finalRelevantEventTypes) {
            userState.mLastSentRelevantEventTypes = finalRelevantEventTypes;
            this.mMainHandler.obtainMessage(12, userState.mUserId, finalRelevantEventTypes);
            this.mMainHandler.post(new com.android.server.accessibility.-$Lambda$kXhldx_ZxidxR4suyNIbZ545MMw.AnonymousClass3(finalRelevantEventTypes, this, userState));
        }
    }

    /* synthetic */ void lambda$-com_android_server_accessibility_AccessibilityManagerService_63307(UserState userState, int finalRelevantEventTypes) {
        broadcastToClients(userState, new com.android.server.accessibility.-$Lambda$kXhldx_ZxidxR4suyNIbZ545MMw.AnonymousClass2(finalRelevantEventTypes));
    }

    static /* synthetic */ void lambda$-com_android_server_accessibility_AccessibilityManagerService_63361(int finalRelevantEventTypes, IAccessibilityManagerClient client) {
        try {
            client.setRelevantEventTypes(finalRelevantEventTypes);
        } catch (RemoteException e) {
        }
    }

    private void broadcastToClients(UserState userState, Consumer<IAccessibilityManagerClient> clientAction) {
        this.mGlobalClients.broadcast(clientAction);
        userState.mUserClients.broadcast(clientAction);
    }

    private boolean doesServiceWantEventLocked(Service service, AccessibilityEvent event) {
        if (!service.canReceiveEventsLocked()) {
            return false;
        }
        if (event.getWindowId() != -1 && (event.isImportantForAccessibility() ^ 1) != 0 && (service.mFetchFlags & 8) == 0) {
            return false;
        }
        int eventType = event.getEventType();
        if ((service.mEventTypes & eventType) != eventType) {
            return false;
        }
        Set<String> packageNames = service.mPackageNames;
        return !packageNames.isEmpty() ? packageNames.contains(event.getPackageName() != null ? event.getPackageName().toString() : null) : true;
    }

    private void unbindAllServicesLocked(UserState userState) {
        List<Service> services = userState.mBoundServices;
        int i = 0;
        int count = services.size();
        while (i < count) {
            if (((Service) services.get(i)).unbindLocked()) {
                i--;
                count--;
            }
            i++;
        }
    }

    private void readComponentNamesFromSettingLocked(String settingName, int userId, Set<ComponentName> outComponentNames) {
        readComponentNamesFromStringLocked(Secure.getStringForUser(this.mContext.getContentResolver(), settingName, userId), outComponentNames, false);
    }

    private void readComponentNamesFromStringLocked(String names, Set<ComponentName> outComponentNames, boolean doMerge) {
        if (!doMerge) {
            outComponentNames.clear();
        }
        if (names != null) {
            SimpleStringSplitter splitter = this.mStringColonSplitter;
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

    private void persistComponentNamesToSettingLocked(String settingName, Set<ComponentName> componentNames, int userId) {
        StringBuilder builder = new StringBuilder();
        for (ComponentName componentName : componentNames) {
            if (builder.length() > 0) {
                builder.append(COMPONENT_NAME_SEPARATOR);
            }
            builder.append(componentName.flattenToShortString());
        }
        long identity = Binder.clearCallingIdentity();
        try {
            Secure.putStringForUser(this.mContext.getContentResolver(), settingName, builder.toString(), userId);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private void updateServicesLocked(UserState userState) {
        Map<ComponentName, Service> componentNameToServiceMap = userState.mComponentNameToServiceMap;
        boolean isUnlockingOrUnlocked = ((UserManagerInternal) LocalServices.getService(UserManagerInternal.class)).isUserUnlockingOrUnlocked(userState.mUserId);
        int i = 0;
        int count = userState.mInstalledServices.size();
        while (i < count && i < userState.mInstalledServices.size()) {
            AccessibilityServiceInfo installedService = (AccessibilityServiceInfo) userState.mInstalledServices.get(i);
            ComponentName componentName = ComponentName.unflattenFromString(installedService.getId());
            Service service = (Service) componentNameToServiceMap.get(componentName);
            if (!isUnlockingOrUnlocked && (installedService.isDirectBootAware() ^ 1) != 0) {
                Slog.d(LOG_TAG, "Ignoring non-encryption-aware service " + componentName);
            } else if (!userState.mBindingServices.contains(componentName)) {
                if (userState.mEnabledServices.contains(componentName)) {
                    if (service == null) {
                        service = new Service(userState.mUserId, componentName, installedService);
                    } else if (userState.mBoundServices.contains(service)) {
                    }
                    service.bindLocked();
                } else if (service != null) {
                    service.unbindLocked();
                }
            }
            i++;
        }
        count = userState.mBoundServices.size();
        this.mTempIntArray.clear();
        for (i = 0; i < count; i++) {
            ResolveInfo resolveInfo = ((Service) userState.mBoundServices.get(i)).mAccessibilityServiceInfo.getResolveInfo();
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
            this.mMainHandler.obtainMessage(2, clientState, userState.mUserId).sendToTarget();
        }
    }

    private void showAccessibilityButtonTargetSelection() {
        Intent intent = new Intent("com.android.internal.intent.action.CHOOSE_ACCESSIBILITY_BUTTON");
        intent.addFlags(268468224);
        this.mContext.startActivityAsUser(intent, UserHandle.of(this.mCurrentUserId));
    }

    private void scheduleNotifyClientsOfServicesStateChange(UserState userState) {
        this.mMainHandler.obtainMessage(10, Integer.valueOf(userState.mUserId)).sendToTarget();
    }

    private void scheduleUpdateInputFilter(UserState userState) {
        this.mMainHandler.obtainMessage(6, userState).sendToTarget();
    }

    private void scheduleUpdateFingerprintGestureHandling(UserState userState) {
        this.mMainHandler.obtainMessage(11, userState).sendToTarget();
    }

    private void updateInputFilter(UserState userState) {
        boolean setInputFilter = false;
        AccessibilityInputFilter inputFilter = null;
        synchronized (this.mLock) {
            int flags = 0;
            if (userState.mIsDisplayMagnificationEnabled) {
                flags = 1;
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
            this.mWindowManagerService.setInputFilter(inputFilter);
        }
    }

    private void showEnableTouchExplorationDialog(final Service service) {
        synchronized (this.mLock) {
            String label = service.mResolveInfo.loadLabel(this.mContext.getPackageManager()).toString();
            final UserState state = getCurrentUserStateLocked();
            if (state.mIsTouchExplorationEnabled) {
            } else if (this.mEnableTouchExplorationDialog == null || !this.mEnableTouchExplorationDialog.isShowing()) {
                this.mEnableTouchExplorationDialog = new Builder(this.mContext).setIconAttribute(16843605).setPositiveButton(17039370, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        state.mTouchExplorationGrantedServices.add(service.mComponentName);
                        AccessibilityManagerService.this.persistComponentNamesToSettingLocked("touch_exploration_granted_accessibility_services", state.mTouchExplorationGrantedServices, state.mUserId);
                        UserState userState = AccessibilityManagerService.this.getUserStateLocked(service.mUserId);
                        userState.mIsTouchExplorationEnabled = true;
                        long identity = Binder.clearCallingIdentity();
                        try {
                            Secure.putIntForUser(AccessibilityManagerService.this.mContext.getContentResolver(), "touch_exploration_enabled", 1, service.mUserId);
                            AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                        } finally {
                            Binder.restoreCallingIdentity(identity);
                        }
                    }
                }).setNegativeButton(17039360, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setTitle(17039943).setMessage(this.mContext.getString(17039942, new Object[]{label})).create();
                this.mEnableTouchExplorationDialog.getWindow().setType(2003);
                LayoutParams attributes = this.mEnableTouchExplorationDialog.getWindow().getAttributes();
                attributes.privateFlags |= 16;
                this.mEnableTouchExplorationDialog.setCanceledOnTouchOutside(true);
                this.mEnableTouchExplorationDialog.show();
            }
        }
    }

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
        List<Service> boundServices = userState.mBoundServices;
        int boundServiceCount = boundServices.size();
        for (int i = 0; i < boundServiceCount; i++) {
            if (((Service) boundServices.get(i)).canRetrieveInteractiveWindowsLocked()) {
                userState.mAccessibilityFocusOnlyInActiveWindow = false;
                return;
            }
        }
        userState.mAccessibilityFocusOnlyInActiveWindow = true;
    }

    private void updateWindowsForAccessibilityCallbackLocked(UserState userState) {
        List<Service> boundServices = userState.mBoundServices;
        int boundServiceCount = boundServices.size();
        for (int i = 0; i < boundServiceCount; i++) {
            if (((Service) boundServices.get(i)).canRetrieveInteractiveWindowsLocked()) {
                if (this.mWindowsForAccessibilityCallback == null) {
                    this.mWindowsForAccessibilityCallback = new WindowsForAccessibilityCallback();
                    this.mWindowManagerService.setWindowsForAccessibilityCallback(this.mWindowsForAccessibilityCallback);
                }
                return;
            }
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
            AccessibilityServiceInfo serviceInfo = (AccessibilityServiceInfo) userState.mInstalledServices.get(i);
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
            if ((((Service) userState.mBoundServices.get(i)).mAccessibilityServiceInfo.getCapabilities() & 32) != 0) {
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
            Service service = (Service) userState.mBoundServices.get(i);
            if (!service.mRequestFilterKeyEvents || (service.mAccessibilityServiceInfo.getCapabilities() & 8) == 0) {
                i++;
            } else {
                userState.mIsFilterKeyEventsEnabled = true;
                return;
            }
        }
        userState.mIsFilterKeyEventsEnabled = false;
    }

    private boolean readConfigurationForUserStateLocked(UserState userState) {
        return (((((((readInstalledAccessibilityServiceLocked(userState) | readEnabledAccessibilityServicesLocked(userState)) | readTouchExplorationGrantedAccessibilityServicesLocked(userState)) | readTouchExplorationEnabledSettingLocked(userState)) | readHighTextContrastEnabledSettingLocked(userState)) | readMagnificationEnabledSettingsLocked(userState)) | readAutoclickEnabledSettingLocked(userState)) | readAccessibilityShortcutSettingLocked(userState)) | readAccessibilityButtonSettingsLocked(userState);
    }

    private void updateAccessibilityEnabledSetting(UserState userState) {
        long identity = Binder.clearCallingIdentity();
        try {
            Secure.putIntForUser(this.mContext.getContentResolver(), "accessibility_enabled", userState.isHandlingAccessibilityEvents() ? 1 : 0, userState.mUserId);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private boolean readTouchExplorationEnabledSettingLocked(UserState userState) {
        boolean touchExplorationEnabled = Secure.getIntForUser(this.mContext.getContentResolver(), "touch_exploration_enabled", 0, userState.mUserId) == 1;
        if (touchExplorationEnabled == userState.mIsTouchExplorationEnabled) {
            return false;
        }
        userState.mIsTouchExplorationEnabled = touchExplorationEnabled;
        return true;
    }

    private boolean readMagnificationEnabledSettingsLocked(UserState userState) {
        boolean displayMagnificationEnabled = Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_display_magnification_enabled", 0, userState.mUserId) == 1;
        boolean navBarMagnificationEnabled = Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_display_magnification_navbar_enabled", 0, userState.mUserId) == 1;
        if (displayMagnificationEnabled == userState.mIsDisplayMagnificationEnabled && navBarMagnificationEnabled == userState.mIsNavBarMagnificationEnabled) {
            return false;
        }
        userState.mIsDisplayMagnificationEnabled = displayMagnificationEnabled;
        userState.mIsNavBarMagnificationEnabled = navBarMagnificationEnabled;
        return true;
    }

    private boolean readAutoclickEnabledSettingLocked(UserState userState) {
        boolean autoclickEnabled = Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_autoclick_enabled", 0, userState.mUserId) == 1;
        if (autoclickEnabled == userState.mIsAutoclickEnabled) {
            return false;
        }
        userState.mIsAutoclickEnabled = autoclickEnabled;
        return true;
    }

    private boolean readHighTextContrastEnabledSettingLocked(UserState userState) {
        boolean highTextContrastEnabled = Secure.getIntForUser(this.mContext.getContentResolver(), "high_text_contrast_enabled", 0, userState.mUserId) == 1;
        if (highTextContrastEnabled == userState.mIsTextHighContrastEnabled) {
            return false;
        }
        userState.mIsTextHighContrastEnabled = highTextContrastEnabled;
        return true;
    }

    private boolean readSoftKeyboardShowModeChangedLocked(UserState userState) {
        int softKeyboardShowMode = Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_soft_keyboard_mode", 0, userState.mUserId);
        if (softKeyboardShowMode == userState.mSoftKeyboardShowMode) {
            return false;
        }
        userState.mSoftKeyboardShowMode = softKeyboardShowMode;
        return true;
    }

    private void updateTouchExplorationLocked(UserState userState) {
        boolean enabled = false;
        int serviceCount = userState.mBoundServices.size();
        for (int i = 0; i < serviceCount; i++) {
            if (canRequestAndRequestsTouchExplorationLocked((Service) userState.mBoundServices.get(i))) {
                enabled = true;
                break;
            }
        }
        if (enabled != userState.mIsTouchExplorationEnabled) {
            userState.mIsTouchExplorationEnabled = enabled;
            long identity = Binder.clearCallingIdentity();
            try {
                Secure.putIntForUser(this.mContext.getContentResolver(), "touch_exploration_enabled", enabled ? 1 : 0, userState.mUserId);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    private boolean readAccessibilityShortcutSettingLocked(UserState userState) {
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

    private boolean readAccessibilityButtonSettingsLocked(UserState userState) {
        String componentId = Secure.getStringForUser(this.mContext.getContentResolver(), "accessibility_button_target_component", userState.mUserId);
        if (TextUtils.isEmpty(componentId)) {
            if (userState.mServiceAssignedToAccessibilityButton == null && (userState.mIsNavBarMagnificationAssignedToAccessibilityButton ^ 1) != 0) {
                return false;
            }
            userState.mServiceAssignedToAccessibilityButton = null;
            userState.mIsNavBarMagnificationAssignedToAccessibilityButton = false;
            return true;
        } else if (!componentId.equals(MagnificationController.class.getName())) {
            ComponentName componentName = ComponentName.unflattenFromString(componentId);
            if (componentName.equals(userState.mServiceAssignedToAccessibilityButton)) {
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
            boolean shortcutServiceIsInstalled = false;
            for (int i = 0; i < userState.mInstalledServices.size(); i++) {
                if (((AccessibilityServiceInfo) userState.mInstalledServices.get(i)).getComponentName().equals(userState.mServiceToEnableWithShortcut)) {
                    shortcutServiceIsInstalled = true;
                }
            }
            if (!shortcutServiceIsInstalled) {
                userState.mServiceToEnableWithShortcut = null;
                long identity = Binder.clearCallingIdentity();
                try {
                    Secure.putStringForUser(this.mContext.getContentResolver(), "accessibility_shortcut_target_service", null, userState.mUserId);
                    Secure.putIntForUser(this.mContext.getContentResolver(), "accessibility_shortcut_enabled", 0, userState.mUserId);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }
    }

    private boolean canRequestAndRequestsTouchExplorationLocked(Service service) {
        if (!service.canReceiveEventsLocked() || (service.mRequestTouchExplorationMode ^ 1) != 0) {
            return false;
        }
        if (service.mIsAutomation) {
            return true;
        }
        if (service.mResolveInfo.serviceInfo.applicationInfo.targetSdkVersion <= 17) {
            if (getUserStateLocked(service.mUserId).mTouchExplorationGrantedServices.contains(service.mComponentName)) {
                return true;
            }
            if (this.mEnableTouchExplorationDialog == null || (this.mEnableTouchExplorationDialog.isShowing() ^ 1) != 0) {
                this.mMainHandler.obtainMessage(7, service).sendToTarget();
            }
        } else if ((service.mAccessibilityServiceInfo.getCapabilities() & 2) != 0) {
            return true;
        }
        return false;
    }

    private void updateDisplayDaltonizerLocked(UserState userState) {
        DisplayAdjustmentUtils.applyDaltonizerSetting(this.mContext, userState.mUserId);
    }

    private void updateDisplayInversionLocked(UserState userState) {
        DisplayAdjustmentUtils.applyInversionSetting(this.mContext, userState.mUserId);
    }

    private void updateMagnificationLocked(UserState userState) {
        if (userState.mUserId == this.mCurrentUserId) {
            if (userState.mIsDisplayMagnificationEnabled || userState.mIsNavBarMagnificationEnabled || userHasListeningMagnificationServicesLocked(userState)) {
                getMagnificationController();
                this.mMagnificationController.register();
            } else if (this.mMagnificationController != null) {
                this.mMagnificationController.unregister();
            }
        }
    }

    private boolean userHasMagnificationServicesLocked(UserState userState) {
        List<Service> services = userState.mBoundServices;
        int count = services.size();
        for (int i = 0; i < count; i++) {
            if (this.mSecurityPolicy.canControlMagnification((Service) services.get(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean userHasListeningMagnificationServicesLocked(UserState userState) {
        List<Service> services = userState.mBoundServices;
        int count = services.size();
        for (int i = 0; i < count; i++) {
            Service service = (Service) services.get(i);
            if (this.mSecurityPolicy.canControlMagnification(service) && service.mInvocationHandler.mIsMagnificationCallbackEnabled) {
                return true;
            }
        }
        return false;
    }

    private void updateSoftKeyboardShowModeLocked(UserState userState) {
        if (userState.mUserId == this.mCurrentUserId && userState.mSoftKeyboardShowMode != 0 && !userState.mEnabledServices.contains(userState.mServiceChangingSoftKeyboardMode)) {
            long identity = Binder.clearCallingIdentity();
            try {
                Secure.putIntForUser(this.mContext.getContentResolver(), "accessibility_soft_keyboard_mode", 0, userState.mUserId);
                userState.mSoftKeyboardShowMode = 0;
                userState.mServiceChangingSoftKeyboardMode = null;
                notifySoftKeyboardShowModeChangedLocked(userState.mSoftKeyboardShowMode);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    private void updateFingerprintGestureHandling(UserState userState) {
        List<Service> services;
        synchronized (this.mLock) {
            services = userState.mBoundServices;
            if (this.mFingerprintGestureDispatcher == null && this.mPackageManager.hasSystemFeature("android.hardware.fingerprint")) {
                int numServices = services.size();
                for (int i = 0; i < numServices; i++) {
                    if (((Service) services.get(i)).isCapturingFingerprintGestures()) {
                        long identity = Binder.clearCallingIdentity();
                        try {
                            IFingerprintService service = IFingerprintService.Stub.asInterface(ServiceManager.getService("fingerprint"));
                            Binder.restoreCallingIdentity(identity);
                            if (service != null) {
                                this.mFingerprintGestureDispatcher = new FingerprintGestureDispatcher(service, this.mLock);
                                break;
                            }
                        } catch (Throwable th) {
                            Binder.restoreCallingIdentity(identity);
                        }
                    }
                }
            }
        }
        if (this.mFingerprintGestureDispatcher != null) {
            this.mFingerprintGestureDispatcher.updateClientList(services);
        }
    }

    private void updateAccessibilityButtonTargetsLocked(UserState userState) {
        for (int i = userState.mBoundServices.size() - 1; i >= 0; i--) {
            Service service = (Service) userState.mBoundServices.get(i);
            if (service.mRequestAccessibilityButton) {
                service.notifyAccessibilityButtonAvailabilityChangedLocked(service.isAccessibilityButtonAvailableLocked(userState));
            }
        }
    }

    @GuardedBy("mLock")
    private MagnificationSpec getCompatibleMagnificationSpecLocked(int windowId) {
        IBinder windowToken = (IBinder) this.mGlobalWindowTokens.get(windowId);
        if (windowToken == null) {
            windowToken = (IBinder) getCurrentUserStateLocked().mWindowTokens.get(windowId);
        }
        if (windowToken != null) {
            return this.mWindowManagerService.getCompatibleMagnificationSpecForWindow(windowToken);
        }
        return null;
    }

    private KeyEventDispatcher getKeyEventDispatcher() {
        if (this.mKeyEventDispatcher == null) {
            this.mKeyEventDispatcher = new KeyEventDispatcher(this.mMainHandler, 8, this.mLock, this.mPowerManager);
        }
        return this.mKeyEventDispatcher;
    }

    public void performAccessibilityShortcut() {
        if (UserHandle.getAppId(Binder.getCallingUid()) == 1000 || this.mContext.checkCallingPermission("android.permission.WRITE_SECURE_SETTINGS") == 0) {
            synchronized (this.mLock) {
                UserState userState = getUserStateLocked(this.mCurrentUserId);
                ComponentName serviceName = userState.mServiceToEnableWithShortcut;
                if (serviceName == null) {
                    return;
                }
                long identity = Binder.clearCallingIdentity();
                try {
                    if (userState.mComponentNameToServiceMap.get(serviceName) == null) {
                        enableAccessibilityServiceLocked(serviceName, this.mCurrentUserId);
                    } else {
                        disableAccessibilityServiceLocked(serviceName, this.mCurrentUserId);
                    }
                    Binder.restoreCallingIdentity(identity);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        } else {
            throw new SecurityException("performAccessibilityShortcut requires the WRITE_SECURE_SETTINGS permission");
        }
    }

    private void enableAccessibilityServiceLocked(ComponentName componentName, int userId) {
        SettingStringHelper setting = new SettingStringHelper(this.mContext.getContentResolver(), "enabled_accessibility_services", userId);
        setting.write(ComponentNameSet.add(setting.read(), componentName));
        UserState userState = getUserStateLocked(userId);
        if (userState.mEnabledServices.add(componentName)) {
            onUserStateChangedLocked(userState);
        }
    }

    private void disableAccessibilityServiceLocked(ComponentName componentName, int userId) {
        SettingStringHelper setting = new SettingStringHelper(this.mContext.getContentResolver(), "enabled_accessibility_services", userId);
        setting.write(ComponentNameSet.remove(setting.read(), componentName));
        UserState userState = getUserStateLocked(userId);
        if (userState.mEnabledServices.remove(componentName)) {
            onUserStateChangedLocked(userState);
        }
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
                int j;
                pw.println("ACCESSIBILITY MANAGER (dumpsys accessibility)");
                pw.println();
                int userCount = this.mUserStates.size();
                for (int i = 0; i < userCount; i++) {
                    UserState userState = (UserState) this.mUserStates.valueAt(i);
                    pw.append("User state[attributes:{id=" + userState.mUserId);
                    pw.append(", currentUser=" + (userState.mUserId == this.mCurrentUserId));
                    pw.append(", touchExplorationEnabled=" + userState.mIsTouchExplorationEnabled);
                    pw.append(", displayMagnificationEnabled=" + userState.mIsDisplayMagnificationEnabled);
                    pw.append(", navBarMagnificationEnabled=" + userState.mIsNavBarMagnificationEnabled);
                    pw.append(", autoclickEnabled=" + userState.mIsAutoclickEnabled);
                    if (userState.mUiAutomationService != null) {
                        pw.append(", ");
                        userState.mUiAutomationService.dump(fd, pw, args);
                        pw.println();
                    }
                    pw.append("}");
                    pw.println();
                    pw.append("           services:{");
                    int serviceCount = userState.mBoundServices.size();
                    for (j = 0; j < serviceCount; j++) {
                        if (j > 0) {
                            pw.append(", ");
                            pw.println();
                            pw.append("                     ");
                        }
                        ((Service) userState.mBoundServices.get(j)).dump(fd, pw, args);
                    }
                    pw.println("}]");
                    pw.println();
                }
                if (this.mSecurityPolicy.mWindows != null) {
                    int windowCount = this.mSecurityPolicy.mWindows.size();
                    for (j = 0; j < windowCount; j++) {
                        if (j > 0) {
                            pw.append(',');
                            pw.println();
                        }
                        pw.append("Window[");
                        pw.append(((AccessibilityWindowInfo) this.mSecurityPolicy.mWindows.get(j)).toString());
                        pw.append(']');
                    }
                }
            }
        }
    }

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

    private void ensureWindowsAvailableTimed() {
        synchronized (this.mLock) {
            if (this.mSecurityPolicy.mWindows != null) {
                return;
            }
            if (this.mWindowsForAccessibilityCallback == null) {
                onUserStateChangedLocked(getCurrentUserStateLocked());
            }
            if (this.mWindowsForAccessibilityCallback == null) {
                return;
            }
            long startMillis = SystemClock.uptimeMillis();
            while (this.mSecurityPolicy.mWindows == null) {
                long remainMillis = 5000 - (SystemClock.uptimeMillis() - startMillis);
                if (remainMillis <= 0) {
                    return;
                }
                try {
                    this.mLock.wait(remainMillis);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    MagnificationController getMagnificationController() {
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
}
