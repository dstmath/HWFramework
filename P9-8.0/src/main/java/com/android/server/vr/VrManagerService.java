package com.android.server.vr;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.AppOpsManager;
import android.app.NotificationManager;
import android.app.Vr2dDisplayProperties;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.display.DisplayManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.service.vr.IPersistentVrStateCallbacks;
import android.service.vr.IVrListener;
import android.service.vr.IVrListener.Stub;
import android.service.vr.IVrManager;
import android.service.vr.IVrStateCallbacks;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.util.DumpUtils;
import com.android.server.LocalServices;
import com.android.server.SystemConfig;
import com.android.server.SystemService;
import com.android.server.utils.ManagedApplicationService;
import com.android.server.utils.ManagedApplicationService.BinderChecker;
import com.android.server.utils.ManagedApplicationService.PendingEvent;
import com.android.server.vr.EnabledComponentsObserver.EnabledComponentChangeListener;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;

public class VrManagerService extends SystemService implements EnabledComponentChangeListener {
    static final boolean DBG = false;
    private static final int EVENT_LOG_SIZE = 32;
    private static final int FLAG_ALL = 3;
    private static final int FLAG_AWAKE = 1;
    private static final int FLAG_NONE = 0;
    private static final int FLAG_SCREEN_ON = 2;
    private static final int INVALID_APPOPS_MODE = -1;
    private static final int MSG_PENDING_VR_STATE_CHANGE = 1;
    private static final int MSG_PERSISTENT_VR_MODE_STATE_CHANGE = 2;
    private static final int MSG_VR_STATE_CHANGE = 0;
    private static final int PENDING_STATE_DELAY_MS = 300;
    public static final String TAG = "VrManagerService";
    private static final BinderChecker sBinderChecker = new BinderChecker() {
        public IInterface asInterface(IBinder binder) {
            return Stub.asInterface(binder);
        }

        public boolean checkType(IInterface service) {
            return service instanceof IVrListener;
        }
    };
    private EnabledComponentsObserver mComponentObserver;
    private Context mContext;
    private ComponentName mCurrentVrModeComponent;
    private int mCurrentVrModeUser;
    private ManagedApplicationService mCurrentVrService;
    private ComponentName mDefaultVrService;
    private boolean mGuard;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            boolean state;
            int i;
            switch (msg.what) {
                case 0:
                    state = msg.arg1 == 1;
                    i = VrManagerService.this.mVrStateRemoteCallbacks.beginBroadcast();
                    while (i > 0) {
                        i--;
                        try {
                            ((IVrStateCallbacks) VrManagerService.this.mVrStateRemoteCallbacks.getBroadcastItem(i)).onVrStateChanged(state);
                        } catch (RemoteException e) {
                        }
                    }
                    VrManagerService.this.mVrStateRemoteCallbacks.finishBroadcast();
                    return;
                case 1:
                    synchronized (VrManagerService.this.mLock) {
                        if (VrManagerService.this.mVrModeAllowed) {
                            VrManagerService.this.consumeAndApplyPendingStateLocked();
                        }
                    }
                    return;
                case 2:
                    state = msg.arg1 == 1;
                    i = VrManagerService.this.mPersistentVrStateRemoteCallbacks.beginBroadcast();
                    while (i > 0) {
                        i--;
                        try {
                            ((IPersistentVrStateCallbacks) VrManagerService.this.mPersistentVrStateRemoteCallbacks.getBroadcastItem(i)).onPersistentVrStateChanged(state);
                        } catch (RemoteException e2) {
                        }
                    }
                    VrManagerService.this.mPersistentVrStateRemoteCallbacks.finishBroadcast();
                    return;
                default:
                    throw new IllegalStateException("Unknown message type: " + msg.what);
            }
        }
    };
    private final Object mLock = new Object();
    private final ArrayDeque<VrState> mLoggingDeque = new ArrayDeque(32);
    private final NotificationAccessManager mNotifAccessManager = new NotificationAccessManager(this, null);
    private final IBinder mOverlayToken = new Binder();
    private VrState mPendingState;
    private boolean mPersistentVrModeEnabled;
    private final RemoteCallbackList<IPersistentVrStateCallbacks> mPersistentVrStateRemoteCallbacks = new RemoteCallbackList();
    private int mPreviousCoarseLocationMode = -1;
    private int mPreviousManageOverlayMode = -1;
    private int mSystemSleepFlags = 1;
    private Vr2dDisplay mVr2dDisplay;
    private final IVrManager mVrManager = new IVrManager.Stub() {
        public void registerListener(IVrStateCallbacks cb) {
            VrManagerService.this.enforceCallerPermission("android.permission.ACCESS_VR_MANAGER");
            if (cb == null) {
                throw new IllegalArgumentException("Callback binder object is null.");
            }
            VrManagerService.this.addStateCallback(cb);
        }

        public void unregisterListener(IVrStateCallbacks cb) {
            VrManagerService.this.enforceCallerPermission("android.permission.ACCESS_VR_MANAGER");
            if (cb == null) {
                throw new IllegalArgumentException("Callback binder object is null.");
            }
            VrManagerService.this.removeStateCallback(cb);
        }

        public void registerPersistentVrStateListener(IPersistentVrStateCallbacks cb) {
            VrManagerService.this.enforceCallerPermission("android.permission.ACCESS_VR_MANAGER");
            if (cb == null) {
                throw new IllegalArgumentException("Callback binder object is null.");
            }
            VrManagerService.this.addPersistentStateCallback(cb);
        }

        public void unregisterPersistentVrStateListener(IPersistentVrStateCallbacks cb) {
            VrManagerService.this.enforceCallerPermission("android.permission.ACCESS_VR_MANAGER");
            if (cb == null) {
                throw new IllegalArgumentException("Callback binder object is null.");
            }
            VrManagerService.this.removePersistentStateCallback(cb);
        }

        public boolean getVrModeState() {
            return VrManagerService.this.getVrMode();
        }

        public void setPersistentVrModeEnabled(boolean enabled) {
            VrManagerService.this.enforceCallerPermission("android.permission.RESTRICTED_VR_ACCESS");
            VrManagerService.this.setPersistentVrModeEnabled(enabled);
        }

        public void setVr2dDisplayProperties(Vr2dDisplayProperties vr2dDisplayProp) {
            VrManagerService.this.enforceCallerPermission("android.permission.RESTRICTED_VR_ACCESS");
            VrManagerService.this.setVr2dDisplayProperties(vr2dDisplayProp);
        }

        public int getVr2dDisplayId() {
            return VrManagerService.this.getVr2dDisplayId();
        }

        protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpPermission(VrManagerService.this.mContext, VrManagerService.TAG, pw)) {
                int i;
                pw.println("********* Dump of VrManagerService *********");
                pw.println("VR mode is currently: " + (VrManagerService.this.mVrModeAllowed ? "allowed" : "disallowed"));
                pw.println("Persistent VR mode is currently: " + (VrManagerService.this.mPersistentVrModeEnabled ? "enabled" : "disabled"));
                pw.println("Previous state transitions:\n");
                String tab = "  ";
                VrManagerService.this.dumpStateTransitions(pw);
                pw.println("\n\nRemote Callbacks:");
                int i2 = VrManagerService.this.mVrStateRemoteCallbacks.beginBroadcast();
                while (true) {
                    i = i2;
                    i2 = i - 1;
                    if (i <= 0) {
                        break;
                    }
                    pw.print(tab);
                    pw.print(VrManagerService.this.mVrStateRemoteCallbacks.getBroadcastItem(i2));
                    if (i2 > 0) {
                        pw.println(",");
                    }
                }
                VrManagerService.this.mVrStateRemoteCallbacks.finishBroadcast();
                pw.println("\n\nPersistent Vr State Remote Callbacks:");
                i2 = VrManagerService.this.mPersistentVrStateRemoteCallbacks.beginBroadcast();
                while (true) {
                    i = i2;
                    i2 = i - 1;
                    if (i <= 0) {
                        break;
                    }
                    pw.print(tab);
                    pw.print(VrManagerService.this.mPersistentVrStateRemoteCallbacks.getBroadcastItem(i2));
                    if (i2 > 0) {
                        pw.println(",");
                    }
                }
                VrManagerService.this.mPersistentVrStateRemoteCallbacks.finishBroadcast();
                pw.println("\n");
                pw.println("Installed VrListenerService components:");
                int userId = VrManagerService.this.mCurrentVrModeUser;
                ArraySet<ComponentName> installed = VrManagerService.this.mComponentObserver.getInstalled(userId);
                if (installed == null || installed.size() == 0) {
                    pw.println("None");
                } else {
                    for (ComponentName n : installed) {
                        pw.print(tab);
                        pw.println(n.flattenToString());
                    }
                }
                pw.println("Enabled VrListenerService components:");
                ArraySet<ComponentName> enabled = VrManagerService.this.mComponentObserver.getEnabled(userId);
                if (enabled == null || enabled.size() == 0) {
                    pw.println("None");
                } else {
                    for (ComponentName n2 : enabled) {
                        pw.print(tab);
                        pw.println(n2.flattenToString());
                    }
                }
                pw.println("\n");
                pw.println("********* End of VrManagerService Dump *********");
            }
        }
    };
    private boolean mVrModeAllowed;
    private boolean mVrModeEnabled;
    private final RemoteCallbackList<IVrStateCallbacks> mVrStateRemoteCallbacks = new RemoteCallbackList();
    private boolean mWasDefaultGranted;

    private final class LocalService extends VrManagerInternal {
        /* synthetic */ LocalService(VrManagerService this$0, LocalService -this1) {
            this();
        }

        private LocalService() {
        }

        public void setVrMode(boolean enabled, ComponentName packageName, int userId, ComponentName callingPackage) {
            VrManagerService.this.setVrMode(enabled, packageName, userId, callingPackage);
        }

        public void onSleepStateChanged(boolean isAsleep) {
            VrManagerService.this.setSleepState(isAsleep);
        }

        public void onScreenStateChanged(boolean isScreenOn) {
            VrManagerService.this.setScreenOn(isScreenOn);
        }

        public boolean isCurrentVrListener(String packageName, int userId) {
            return VrManagerService.this.isCurrentVrListener(packageName, userId);
        }

        public int hasVrPackage(ComponentName packageName, int userId) {
            return VrManagerService.this.hasVrPackage(packageName, userId);
        }

        public void setPersistentVrModeEnabled(boolean enabled) {
            VrManagerService.this.setPersistentVrModeEnabled(enabled);
        }

        public void setVr2dDisplayProperties(Vr2dDisplayProperties compatDisplayProp) {
            VrManagerService.this.setVr2dDisplayProperties(compatDisplayProp);
        }

        public int getVr2dDisplayId() {
            return VrManagerService.this.getVr2dDisplayId();
        }

        public void addPersistentVrModeStateListener(IPersistentVrStateCallbacks listener) {
            VrManagerService.this.addPersistentStateCallback(listener);
        }
    }

    private final class NotificationAccessManager {
        private final SparseArray<ArraySet<String>> mAllowedPackages;
        private final ArrayMap<String, Integer> mNotificationAccessPackageToUserId;

        /* synthetic */ NotificationAccessManager(VrManagerService this$0, NotificationAccessManager -this1) {
            this();
        }

        private NotificationAccessManager() {
            this.mAllowedPackages = new SparseArray();
            this.mNotificationAccessPackageToUserId = new ArrayMap();
        }

        public void update(Collection<String> packageNames) {
            int currentUserId = ActivityManager.getCurrentUser();
            ArraySet<String> allowed = (ArraySet) this.mAllowedPackages.get(currentUserId);
            if (allowed == null) {
                allowed = new ArraySet();
            }
            for (int i = this.mNotificationAccessPackageToUserId.size() - 1; i >= 0; i--) {
                int grantUserId = ((Integer) this.mNotificationAccessPackageToUserId.valueAt(i)).intValue();
                if (grantUserId != currentUserId) {
                    String packageName = (String) this.mNotificationAccessPackageToUserId.keyAt(i);
                    VrManagerService.this.revokeNotificationListenerAccess(packageName, grantUserId);
                    VrManagerService.this.revokeNotificationPolicyAccess(packageName);
                    VrManagerService.this.revokeCoarseLocationPermissionIfNeeded(packageName, grantUserId);
                    this.mNotificationAccessPackageToUserId.removeAt(i);
                }
            }
            for (String pkg : allowed) {
                if (!packageNames.contains(pkg)) {
                    VrManagerService.this.revokeNotificationListenerAccess(pkg, currentUserId);
                    VrManagerService.this.revokeNotificationPolicyAccess(pkg);
                    VrManagerService.this.revokeCoarseLocationPermissionIfNeeded(pkg, currentUserId);
                    this.mNotificationAccessPackageToUserId.remove(pkg);
                }
            }
            for (String pkg2 : packageNames) {
                if (!allowed.contains(pkg2)) {
                    VrManagerService.this.grantNotificationPolicyAccess(pkg2);
                    VrManagerService.this.grantNotificationListenerAccess(pkg2, currentUserId);
                    VrManagerService.this.grantCoarseLocationPermissionIfNeeded(pkg2, currentUserId);
                    this.mNotificationAccessPackageToUserId.put(pkg2, Integer.valueOf(currentUserId));
                }
            }
            allowed.clear();
            allowed.addAll(packageNames);
            this.mAllowedPackages.put(currentUserId, allowed);
        }
    }

    private static class VrState {
        final ComponentName callingPackage;
        final boolean defaultPermissionsGranted;
        final boolean enabled;
        final ComponentName targetPackageName;
        final long timestamp;
        final int userId;

        VrState(boolean enabled, ComponentName targetPackageName, int userId, ComponentName callingPackage) {
            this.enabled = enabled;
            this.userId = userId;
            this.targetPackageName = targetPackageName;
            this.callingPackage = callingPackage;
            this.defaultPermissionsGranted = false;
            this.timestamp = System.currentTimeMillis();
        }

        VrState(boolean enabled, ComponentName targetPackageName, int userId, ComponentName callingPackage, boolean defaultPermissionsGranted) {
            this.enabled = enabled;
            this.userId = userId;
            this.targetPackageName = targetPackageName;
            this.callingPackage = callingPackage;
            this.defaultPermissionsGranted = defaultPermissionsGranted;
            this.timestamp = System.currentTimeMillis();
        }
    }

    private static native void initializeNative();

    private static native void setVrModeNative(boolean z);

    private void setVrModeAllowedLocked(boolean allowed) {
        if (this.mVrModeAllowed != allowed) {
            this.mVrModeAllowed = allowed;
            if (this.mVrModeAllowed) {
                consumeAndApplyPendingStateLocked();
                return;
            }
            VrState vrState;
            setPersistentModeAndNotifyListenersLocked(false);
            if (!this.mVrModeEnabled || this.mCurrentVrService == null) {
                vrState = null;
            } else {
                vrState = new VrState(this.mVrModeEnabled, this.mCurrentVrService.getComponent(), this.mCurrentVrService.getUserId(), this.mCurrentVrModeComponent);
            }
            this.mPendingState = vrState;
            updateCurrentVrServiceLocked(false, null, 0, null);
        }
    }

    private void setSleepState(boolean isAsleep) {
        synchronized (this.mLock) {
            if (isAsleep) {
                this.mSystemSleepFlags &= -2;
            } else {
                this.mSystemSleepFlags |= 1;
            }
            setVrModeAllowedLocked(this.mSystemSleepFlags == 3);
        }
    }

    private void setScreenOn(boolean isScreenOn) {
        synchronized (this.mLock) {
            if (isScreenOn) {
                this.mSystemSleepFlags |= 2;
            } else {
                this.mSystemSleepFlags &= -3;
            }
            setVrModeAllowedLocked(this.mSystemSleepFlags == 3);
        }
    }

    public void onEnabledComponentChanged() {
        synchronized (this.mLock) {
            ArraySet<ComponentName> enabledListeners = this.mComponentObserver.getEnabled(ActivityManager.getCurrentUser());
            if (enabledListeners == null) {
                return;
            }
            ArraySet<String> enabledPackages = new ArraySet();
            for (ComponentName n : enabledListeners) {
                if (isDefaultAllowed(n.getPackageName())) {
                    enabledPackages.add(n.getPackageName());
                }
            }
            this.mNotifAccessManager.update(enabledPackages);
            if (this.mVrModeAllowed) {
                consumeAndApplyPendingStateLocked(false);
                if (this.mCurrentVrService == null) {
                    return;
                }
                updateCurrentVrServiceLocked(this.mVrModeEnabled, this.mCurrentVrService.getComponent(), this.mCurrentVrService.getUserId(), this.mCurrentVrModeComponent);
                return;
            }
        }
    }

    private void enforceCallerPermission(String permission) {
        if (this.mContext.checkCallingOrSelfPermission(permission) != 0) {
            throw new SecurityException("Caller does not hold the permission " + permission);
        }
    }

    public VrManagerService(Context context) {
        super(context);
    }

    public void onStart() {
        synchronized (this.mLock) {
            initializeNative();
            this.mContext = getContext();
        }
        -wrap2(VrManagerInternal.class, new LocalService(this, null));
        publishBinderService("vrmanager", this.mVrManager.asBinder());
    }

    public void onBootPhase(int phase) {
        if (phase == 500) {
            synchronized (this.mLock) {
                Looper looper = Looper.getMainLooper();
                Handler handler = new Handler(looper);
                ArrayList<EnabledComponentChangeListener> listeners = new ArrayList();
                listeners.add(this);
                this.mComponentObserver = EnabledComponentsObserver.build(this.mContext, handler, "enabled_vr_listeners", looper, "android.permission.BIND_VR_LISTENER_SERVICE", "android.service.vr.VrListenerService", this.mLock, listeners);
                this.mComponentObserver.rebuildAll();
            }
            ArraySet<ComponentName> defaultVrComponents = SystemConfig.getInstance().getDefaultVrComponents();
            if (defaultVrComponents.size() > 0) {
                this.mDefaultVrService = (ComponentName) defaultVrComponents.valueAt(0);
            } else {
                Slog.i(TAG, "No default vr listener service found.");
            }
            this.mVr2dDisplay = new Vr2dDisplay((DisplayManager) getContext().getSystemService("display"), (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class), this.mVrManager);
            this.mVr2dDisplay.init(getContext());
        } else if (phase == 600) {
            synchronized (this.mLock) {
                this.mVrModeAllowed = true;
            }
        }
    }

    public void onStartUser(int userHandle) {
        synchronized (this.mLock) {
            this.mComponentObserver.onUsersChanged();
        }
    }

    public void onSwitchUser(int userHandle) {
        synchronized (this.mLock) {
            this.mComponentObserver.onUsersChanged();
        }
    }

    public void onStopUser(int userHandle) {
        synchronized (this.mLock) {
            this.mComponentObserver.onUsersChanged();
        }
    }

    public void onCleanupUser(int userHandle) {
        synchronized (this.mLock) {
            this.mComponentObserver.onUsersChanged();
        }
    }

    private void updateOverlayStateLocked(String exemptedPackage, int newUserId, int oldUserId) {
        AppOpsManager appOpsManager = (AppOpsManager) getContext().getSystemService(AppOpsManager.class);
        if (oldUserId != newUserId) {
            appOpsManager.setUserRestrictionForUser(24, false, this.mOverlayToken, null, oldUserId);
        }
        appOpsManager.setUserRestrictionForUser(24, this.mVrModeEnabled, this.mOverlayToken, exemptedPackage == null ? new String[0] : new String[]{exemptedPackage}, newUserId);
    }

    private void updateDependentAppOpsLocked(String newVrServicePackage, int newUserId, String oldVrServicePackage, int oldUserId) {
        if (!Objects.equals(newVrServicePackage, oldVrServicePackage)) {
            long identity = Binder.clearCallingIdentity();
            try {
                updateOverlayStateLocked(newVrServicePackage, newUserId, oldUserId);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    private boolean updateCurrentVrServiceLocked(boolean enabled, ComponentName component, int userId, ComponentName calling) {
        boolean sendUpdatedCaller = false;
        long identity = Binder.clearCallingIdentity();
        try {
            boolean validUserComponent = this.mComponentObserver.isValid(component, userId) == 0;
            boolean goingIntoVrMode = validUserComponent ? enabled : false;
            if (!this.mVrModeEnabled && (goingIntoVrMode ^ 1) != 0) {
                return validUserComponent;
            }
            String oldVrServicePackage = this.mCurrentVrService != null ? this.mCurrentVrService.getComponent().getPackageName() : null;
            int oldUserId = this.mCurrentVrModeUser;
            changeVrModeLocked(goingIntoVrMode);
            boolean nothingChanged = false;
            if (goingIntoVrMode) {
                if (this.mCurrentVrService == null) {
                    createAndConnectService(component, userId);
                    sendUpdatedCaller = true;
                } else if (this.mCurrentVrService.disconnectIfNotMatching(component, userId)) {
                    Slog.i(TAG, "VR mode component changed to " + component + ", disconnecting " + this.mCurrentVrService.getComponent() + " for user " + this.mCurrentVrService.getUserId());
                    createAndConnectService(component, userId);
                    sendUpdatedCaller = true;
                } else {
                    nothingChanged = true;
                }
            } else if (this.mCurrentVrService != null) {
                Slog.i(TAG, "Leaving VR mode, disconnecting " + this.mCurrentVrService.getComponent() + " for user " + this.mCurrentVrService.getUserId());
                this.mCurrentVrService.disconnect();
                this.mCurrentVrService = null;
            } else {
                nothingChanged = true;
            }
            if (calling != null || this.mPersistentVrModeEnabled) {
                if ((Objects.equals(calling, this.mCurrentVrModeComponent) ^ 1) != 0) {
                    sendUpdatedCaller = true;
                }
            }
            this.mCurrentVrModeComponent = calling;
            if (this.mCurrentVrModeUser != userId) {
                this.mCurrentVrModeUser = userId;
                sendUpdatedCaller = true;
            }
            updateDependentAppOpsLocked(this.mCurrentVrService != null ? this.mCurrentVrService.getComponent().getPackageName() : null, this.mCurrentVrModeUser, oldVrServicePackage, oldUserId);
            if (this.mCurrentVrService != null && sendUpdatedCaller) {
                final ComponentName c = this.mCurrentVrModeComponent;
                this.mCurrentVrService.sendEvent(new PendingEvent() {
                    public void runEvent(IInterface service) throws RemoteException {
                        ((IVrListener) service).focusedActivityChanged(c);
                    }
                });
            }
            if (!nothingChanged) {
                logStateLocked();
            }
            Binder.restoreCallingIdentity(identity);
            return validUserComponent;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private boolean isDefaultAllowed(String packageName) {
        ApplicationInfo info = null;
        try {
            info = this.mContext.getPackageManager().getApplicationInfo(packageName, 128);
        } catch (NameNotFoundException e) {
        }
        if (info != null) {
            int i;
            if (info.isSystemApp()) {
                i = 1;
            } else {
                i = info.isUpdatedSystemApp();
            }
            if ((i ^ 1) == 0) {
                return true;
            }
        }
        return false;
    }

    private void grantNotificationPolicyAccess(String pkg) {
        ((NotificationManager) this.mContext.getSystemService(NotificationManager.class)).setNotificationPolicyAccessGranted(pkg, true);
    }

    private void revokeNotificationPolicyAccess(String pkg) {
        NotificationManager nm = (NotificationManager) this.mContext.getSystemService(NotificationManager.class);
        nm.removeAutomaticZenRules(pkg);
        nm.setNotificationPolicyAccessGranted(pkg, false);
    }

    private void grantNotificationListenerAccess(String pkg, int userId) {
        ArraySet<ComponentName> possibleServices = EnabledComponentsObserver.loadComponentNames(this.mContext.getPackageManager(), userId, "android.service.notification.NotificationListenerService", "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE");
        ContentResolver resolver = this.mContext.getContentResolver();
        ArraySet<String> current = getNotificationListeners(resolver, userId);
        for (ComponentName c : possibleServices) {
            String flatName = c.flattenToString();
            if (Objects.equals(c.getPackageName(), pkg) && (current.contains(flatName) ^ 1) != 0) {
                current.add(flatName);
            }
        }
        if (current.size() > 0) {
            Secure.putStringForUser(resolver, "enabled_notification_listeners", formatSettings(current), userId);
        }
    }

    private void revokeNotificationListenerAccess(String pkg, int userId) {
        ContentResolver resolver = this.mContext.getContentResolver();
        ArraySet<String> current = getNotificationListeners(resolver, userId);
        ArrayList<String> toRemove = new ArrayList();
        for (String c : current) {
            ComponentName component = ComponentName.unflattenFromString(c);
            if (component != null && component.getPackageName().equals(pkg)) {
                toRemove.add(c);
            }
        }
        current.removeAll(toRemove);
        Secure.putStringForUser(resolver, "enabled_notification_listeners", formatSettings(current), userId);
    }

    private void grantCoarseLocationPermissionIfNeeded(String pkg, int userId) {
        if (!isPermissionUserUpdated("android.permission.ACCESS_COARSE_LOCATION", pkg, userId)) {
            try {
                this.mContext.getPackageManager().grantRuntimePermission(pkg, "android.permission.ACCESS_COARSE_LOCATION", new UserHandle(userId));
            } catch (IllegalArgumentException e) {
                Slog.w(TAG, "Could not grant coarse location permission, package " + pkg + " was removed.");
            }
        }
    }

    private void revokeCoarseLocationPermissionIfNeeded(String pkg, int userId) {
        if (!isPermissionUserUpdated("android.permission.ACCESS_COARSE_LOCATION", pkg, userId)) {
            try {
                this.mContext.getPackageManager().revokeRuntimePermission(pkg, "android.permission.ACCESS_COARSE_LOCATION", new UserHandle(userId));
            } catch (IllegalArgumentException e) {
                Slog.w(TAG, "Could not revoke coarse location permission, package " + pkg + " was removed.");
            }
        }
    }

    private boolean isPermissionUserUpdated(String permission, String pkg, int userId) {
        if ((this.mContext.getPackageManager().getPermissionFlags(permission, pkg, new UserHandle(userId)) & 3) != 0) {
            return true;
        }
        return false;
    }

    private ArraySet<String> getNotificationListeners(ContentResolver resolver, int userId) {
        String flat = Secure.getStringForUser(resolver, "enabled_notification_listeners", userId);
        ArraySet<String> current = new ArraySet();
        if (flat != null) {
            for (String s : flat.split(":")) {
                if (!TextUtils.isEmpty(s)) {
                    current.add(s);
                }
            }
        }
        return current;
    }

    private static String formatSettings(Collection<String> c) {
        if (c == null || c.isEmpty()) {
            return "";
        }
        StringBuilder b = new StringBuilder();
        boolean start = true;
        for (String s : c) {
            if (!"".equals(s)) {
                if (!start) {
                    b.append(':');
                }
                b.append(s);
                start = false;
            }
        }
        return b.toString();
    }

    private void createAndConnectService(ComponentName component, int userId) {
        this.mCurrentVrService = create(this.mContext, component, userId);
        this.mCurrentVrService.connect();
        Slog.i(TAG, "Connecting " + component + " for user " + userId);
    }

    private void changeVrModeLocked(boolean enabled) {
        if (this.mVrModeEnabled != enabled) {
            this.mVrModeEnabled = enabled;
            Slog.i(TAG, "VR mode " + (this.mVrModeEnabled ? "enabled" : "disabled"));
            setVrModeNative(this.mVrModeEnabled);
            setHwEnviroment(enabled);
            onVrModeChangedLocked();
        }
    }

    private void setHwEnviroment(boolean enabled) {
        SystemProperties.set("persist.sys.ui.hw", enabled ? "true" : "false");
    }

    private void onVrModeChangedLocked() {
        int i;
        Handler handler = this.mHandler;
        Handler handler2 = this.mHandler;
        if (this.mVrModeEnabled) {
            i = 1;
        } else {
            i = 0;
        }
        handler.sendMessage(handler2.obtainMessage(0, i, 0));
    }

    private static ManagedApplicationService create(Context context, ComponentName component, int userId) {
        return ManagedApplicationService.build(context, component, userId, 17041203, "android.settings.VR_LISTENER_SETTINGS", sBinderChecker);
    }

    private void consumeAndApplyPendingStateLocked() {
        consumeAndApplyPendingStateLocked(true);
    }

    private void consumeAndApplyPendingStateLocked(boolean disconnectIfNoPendingState) {
        if (this.mPendingState != null) {
            updateCurrentVrServiceLocked(this.mPendingState.enabled, this.mPendingState.targetPackageName, this.mPendingState.userId, this.mPendingState.callingPackage);
            this.mPendingState = null;
        } else if (disconnectIfNoPendingState) {
            updateCurrentVrServiceLocked(false, null, 0, null);
        }
    }

    private void logStateLocked() {
        ComponentName currentBoundService;
        if (this.mCurrentVrService == null) {
            currentBoundService = null;
        } else {
            currentBoundService = this.mCurrentVrService.getComponent();
        }
        VrState current = new VrState(this.mVrModeEnabled, currentBoundService, this.mCurrentVrModeUser, this.mCurrentVrModeComponent, this.mWasDefaultGranted);
        if (this.mLoggingDeque.size() == 32) {
            this.mLoggingDeque.removeFirst();
        }
        this.mLoggingDeque.add(current);
    }

    private void dumpStateTransitions(PrintWriter pw) {
        SimpleDateFormat d = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
        String tab = "  ";
        if (this.mLoggingDeque.size() == 0) {
            pw.print(tab);
            pw.println("None");
        }
        for (VrState state : this.mLoggingDeque) {
            pw.print(d.format(new Date(state.timestamp)));
            pw.print(tab);
            pw.print("State changed to:");
            pw.print(tab);
            pw.println(state.enabled ? "ENABLED" : "DISABLED");
            if (state.enabled) {
                pw.print(tab);
                pw.print("User=");
                pw.println(state.userId);
                pw.print(tab);
                pw.print("Current VR Activity=");
                pw.println(state.callingPackage == null ? "None" : state.callingPackage.flattenToString());
                pw.print(tab);
                pw.print("Bound VrListenerService=");
                pw.println(state.targetPackageName == null ? "None" : state.targetPackageName.flattenToString());
                if (state.defaultPermissionsGranted) {
                    pw.print(tab);
                    pw.println("Default permissions granted to the bound VrListenerService.");
                }
            }
        }
    }

    private void setVrMode(boolean enabled, ComponentName targetPackageName, int userId, ComponentName callingPackage) {
        synchronized (this.mLock) {
            ComponentName targetListener;
            ComponentName foregroundVrComponent;
            boolean targetEnabledState = !enabled ? this.mPersistentVrModeEnabled : true;
            if (enabled || !this.mPersistentVrModeEnabled) {
                targetListener = targetPackageName;
                foregroundVrComponent = callingPackage;
            } else {
                targetListener = this.mDefaultVrService;
                foregroundVrComponent = null;
            }
            VrState pending = new VrState(targetEnabledState, targetListener, userId, foregroundVrComponent);
            if (this.mVrModeAllowed) {
                if (!targetEnabledState) {
                    if (this.mCurrentVrService != null) {
                        if (this.mPendingState == null) {
                            this.mHandler.sendEmptyMessageDelayed(1, 300);
                        }
                        this.mPendingState = pending;
                        return;
                    }
                }
                this.mHandler.removeMessages(1);
                this.mPendingState = null;
                if (targetPackageName == null) {
                    return;
                }
                updateCurrentVrServiceLocked(targetEnabledState, targetListener, userId, foregroundVrComponent);
                return;
            }
            this.mPendingState = pending;
        }
    }

    private void setPersistentVrModeEnabled(boolean enabled) {
        synchronized (this.mLock) {
            setPersistentModeAndNotifyListenersLocked(enabled);
            if (!enabled && this.mCurrentVrModeComponent == null) {
                setVrMode(false, null, 0, null);
            }
        }
    }

    public void setVr2dDisplayProperties(Vr2dDisplayProperties compatDisplayProp) {
        if (this.mVr2dDisplay != null) {
            this.mVr2dDisplay.setVirtualDisplayProperties(compatDisplayProp);
        } else {
            Slog.w(TAG, "Vr2dDisplay is null!");
        }
    }

    private int getVr2dDisplayId() {
        if (this.mVr2dDisplay != null) {
            return this.mVr2dDisplay.getVirtualDisplayId();
        }
        Slog.w(TAG, "Vr2dDisplay is null!");
        return -1;
    }

    private void setPersistentModeAndNotifyListenersLocked(boolean enabled) {
        if (this.mPersistentVrModeEnabled != enabled) {
            int i;
            this.mPersistentVrModeEnabled = enabled;
            Handler handler = this.mHandler;
            Handler handler2 = this.mHandler;
            if (this.mPersistentVrModeEnabled) {
                i = 1;
            } else {
                i = 0;
            }
            handler.sendMessage(handler2.obtainMessage(2, i, 0));
        }
    }

    private int hasVrPackage(ComponentName targetPackageName, int userId) {
        int isValid;
        synchronized (this.mLock) {
            isValid = this.mComponentObserver.isValid(targetPackageName, userId);
        }
        return isValid;
    }

    /* JADX WARNING: Missing block: B:14:0x0024, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isCurrentVrListener(String packageName, int userId) {
        boolean z = false;
        synchronized (this.mLock) {
            if (this.mCurrentVrService == null) {
                return false;
            } else if (this.mCurrentVrService.getComponent().getPackageName().equals(packageName) && userId == this.mCurrentVrService.getUserId()) {
                z = true;
            }
        }
    }

    private void addStateCallback(IVrStateCallbacks cb) {
        this.mVrStateRemoteCallbacks.register(cb);
    }

    private void removeStateCallback(IVrStateCallbacks cb) {
        this.mVrStateRemoteCallbacks.unregister(cb);
    }

    private void addPersistentStateCallback(IPersistentVrStateCallbacks cb) {
        this.mPersistentVrStateRemoteCallbacks.register(cb);
    }

    private void removePersistentStateCallback(IPersistentVrStateCallbacks cb) {
        this.mPersistentVrStateRemoteCallbacks.unregister(cb);
    }

    private boolean getVrMode() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mVrModeEnabled;
        }
        return z;
    }
}
