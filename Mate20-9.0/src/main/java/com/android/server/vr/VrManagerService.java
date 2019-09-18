package com.android.server.vr;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.AppOpsManager;
import android.app.INotificationManager;
import android.app.NotificationManager;
import android.app.Vr2dDisplayProperties;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.service.vr.IPersistentVrStateCallbacks;
import android.service.vr.IVrListener;
import android.service.vr.IVrManager;
import android.service.vr.IVrStateCallbacks;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import android.util.SparseArray;
import android.view.inputmethod.InputMethodManagerInternal;
import com.android.internal.util.DumpUtils;
import com.android.server.FgThread;
import com.android.server.HwServiceExFactory;
import com.android.server.LocalServices;
import com.android.server.SystemConfig;
import com.android.server.SystemService;
import com.android.server.utils.ManagedApplicationService;
import com.android.server.vr.EnabledComponentsObserver;
import com.android.server.wm.WindowManagerInternal;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Objects;

public class VrManagerService extends SystemService implements EnabledComponentsObserver.EnabledComponentChangeListener, ActivityManagerInternal.ScreenObserver {
    static final boolean DBG = false;
    private static final int EVENT_LOG_SIZE = 64;
    private static final int FLAG_ALL = 7;
    private static final int FLAG_AWAKE = 1;
    private static final int FLAG_KEYGUARD_UNLOCKED = 4;
    private static final int FLAG_NONE = 0;
    private static final int FLAG_SCREEN_ON = 2;
    private static final int INVALID_APPOPS_MODE = -1;
    private static final int MSG_PENDING_VR_STATE_CHANGE = 1;
    private static final int MSG_PERSISTENT_VR_MODE_STATE_CHANGE = 2;
    private static final int MSG_VR_STATE_CHANGE = 0;
    private static final int PENDING_STATE_DELAY_MS = 300;
    public static final String TAG = "VrManagerService";
    private static final ManagedApplicationService.BinderChecker sBinderChecker = new ManagedApplicationService.BinderChecker() {
        public IInterface asInterface(IBinder binder) {
            return IVrListener.Stub.asInterface(binder);
        }

        public boolean checkType(IInterface service) {
            return service instanceof IVrListener;
        }
    };
    /* access modifiers changed from: private */
    public boolean mBootsToVr;
    /* access modifiers changed from: private */
    public EnabledComponentsObserver mComponentObserver;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public ManagedApplicationService mCurrentVrCompositorService;
    private ComponentName mCurrentVrModeComponent;
    /* access modifiers changed from: private */
    public int mCurrentVrModeUser;
    /* access modifiers changed from: private */
    public ManagedApplicationService mCurrentVrService;
    private ComponentName mDefaultVrService;
    private final ManagedApplicationService.EventCallback mEventCallback;
    private boolean mGuard;
    private final Handler mHandler;
    private IHwVrManagerServiceEx mHwVMSEx;
    /* access modifiers changed from: private */
    public final Object mLock;
    private boolean mLogLimitHit;
    private final ArrayDeque<ManagedApplicationService.LogFormattable> mLoggingDeque;
    private final NotificationAccessManager mNotifAccessManager;
    private INotificationManager mNotificationManager;
    private final IBinder mOverlayToken;
    private VrState mPendingState;
    /* access modifiers changed from: private */
    public boolean mPersistentVrModeEnabled;
    /* access modifiers changed from: private */
    public final RemoteCallbackList<IPersistentVrStateCallbacks> mPersistentVrStateRemoteCallbacks;
    private int mPreviousCoarseLocationMode;
    private int mPreviousManageOverlayMode;
    private boolean mRunning2dInVr;
    private boolean mStandby;
    private int mSystemSleepFlags;
    private boolean mUseStandbyToExitVrMode;
    private boolean mUserUnlocked;
    private Vr2dDisplay mVr2dDisplay;
    private int mVrAppProcessId;
    private final IVrManager mVrManager;
    /* access modifiers changed from: private */
    public boolean mVrModeAllowed;
    private boolean mVrModeEnabled;
    /* access modifiers changed from: private */
    public final RemoteCallbackList<IVrStateCallbacks> mVrStateRemoteCallbacks;
    private boolean mWasDefaultGranted;

    private final class LocalService extends VrManagerInternal {
        private LocalService() {
        }

        public void setVrMode(boolean enabled, ComponentName packageName, int userId, int processId, ComponentName callingPackage) {
            VrManagerService.this.setVrMode(enabled, packageName, userId, processId, callingPackage);
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

        private NotificationAccessManager() {
            this.mAllowedPackages = new SparseArray<>();
            this.mNotificationAccessPackageToUserId = new ArrayMap<>();
        }

        public void update(Collection<String> packageNames) {
            int currentUserId = ActivityManager.getCurrentUser();
            ArraySet<String> allowed = this.mAllowedPackages.get(currentUserId);
            if (allowed == null) {
                allowed = new ArraySet<>();
            }
            for (int i = this.mNotificationAccessPackageToUserId.size() - 1; i >= 0; i--) {
                int grantUserId = this.mNotificationAccessPackageToUserId.valueAt(i).intValue();
                if (grantUserId != currentUserId) {
                    String packageName = this.mNotificationAccessPackageToUserId.keyAt(i);
                    VrManagerService.this.revokeNotificationListenerAccess(packageName, grantUserId);
                    VrManagerService.this.revokeNotificationPolicyAccess(packageName);
                    VrManagerService.this.revokeCoarseLocationPermissionIfNeeded(packageName, grantUserId);
                    this.mNotificationAccessPackageToUserId.removeAt(i);
                }
            }
            Iterator<String> it = allowed.iterator();
            while (it.hasNext()) {
                String pkg = it.next();
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

    private static class SettingEvent implements ManagedApplicationService.LogFormattable {
        public final long timestamp = System.currentTimeMillis();
        public final String what;

        SettingEvent(String what2) {
            this.what = what2;
        }

        public String toLogString(SimpleDateFormat dateFormat) {
            return dateFormat.format(new Date(this.timestamp)) + "   " + this.what;
        }
    }

    private static class VrState implements ManagedApplicationService.LogFormattable {
        final ComponentName callingPackage;
        final boolean defaultPermissionsGranted;
        final boolean enabled;
        final int processId;
        final boolean running2dInVr;
        final ComponentName targetPackageName;
        final long timestamp;
        final int userId;

        VrState(boolean enabled2, boolean running2dInVr2, ComponentName targetPackageName2, int userId2, int processId2, ComponentName callingPackage2) {
            this.enabled = enabled2;
            this.running2dInVr = running2dInVr2;
            this.userId = userId2;
            this.processId = processId2;
            this.targetPackageName = targetPackageName2;
            this.callingPackage = callingPackage2;
            this.defaultPermissionsGranted = false;
            this.timestamp = System.currentTimeMillis();
        }

        VrState(boolean enabled2, boolean running2dInVr2, ComponentName targetPackageName2, int userId2, int processId2, ComponentName callingPackage2, boolean defaultPermissionsGranted2) {
            this.enabled = enabled2;
            this.running2dInVr = running2dInVr2;
            this.userId = userId2;
            this.processId = processId2;
            this.targetPackageName = targetPackageName2;
            this.callingPackage = callingPackage2;
            this.defaultPermissionsGranted = defaultPermissionsGranted2;
            this.timestamp = System.currentTimeMillis();
        }

        public String toLogString(SimpleDateFormat dateFormat) {
            String str;
            StringBuilder sb = new StringBuilder(dateFormat.format(new Date(this.timestamp)));
            sb.append("  ");
            sb.append("State changed to:");
            sb.append("  ");
            sb.append(this.enabled ? "ENABLED" : "DISABLED");
            sb.append("\n");
            if (this.enabled) {
                sb.append("  ");
                sb.append("User=");
                sb.append(this.userId);
                sb.append("\n");
                sb.append("  ");
                sb.append("Current VR Activity=");
                sb.append(this.callingPackage == null ? "None" : this.callingPackage.flattenToString());
                sb.append("\n");
                sb.append("  ");
                sb.append("Bound VrListenerService=");
                if (this.targetPackageName == null) {
                    str = "None";
                } else {
                    str = this.targetPackageName.flattenToString();
                }
                sb.append(str);
                sb.append("\n");
                if (this.defaultPermissionsGranted) {
                    sb.append("  ");
                    sb.append("Default permissions granted to the bound VrListenerService.");
                    sb.append("\n");
                }
            }
            return sb.toString();
        }
    }

    private static native void initializeNative();

    private static native void setVrModeNative(boolean z);

    private void updateVrModeAllowedLocked() {
        VrState vrState;
        boolean allowed = (this.mSystemSleepFlags == 7 || (this.mBootsToVr && this.mUseStandbyToExitVrMode)) && this.mUserUnlocked && !(this.mStandby && this.mUseStandbyToExitVrMode);
        if (this.mVrModeAllowed != allowed) {
            this.mVrModeAllowed = allowed;
            if (this.mVrModeAllowed) {
                if (this.mBootsToVr) {
                    setPersistentVrModeEnabled(true);
                }
                if (this.mBootsToVr && !this.mVrModeEnabled) {
                    setVrMode(true, this.mDefaultVrService, 0, -1, null);
                    return;
                }
                return;
            }
            setPersistentModeAndNotifyListenersLocked(false);
            if (!this.mVrModeEnabled || this.mCurrentVrService == null) {
                vrState = null;
            } else {
                vrState = new VrState(this.mVrModeEnabled, this.mRunning2dInVr, this.mCurrentVrService.getComponent(), this.mCurrentVrService.getUserId(), this.mVrAppProcessId, this.mCurrentVrModeComponent);
            }
            this.mPendingState = vrState;
            updateCurrentVrServiceLocked(false, false, null, 0, -1, null);
        }
    }

    /* access modifiers changed from: private */
    public void setScreenOn(boolean isScreenOn) {
        setSystemState(2, isScreenOn);
    }

    public void onAwakeStateChanged(boolean isAwake) {
        setSystemState(1, isAwake);
    }

    public void onKeyguardStateChanged(boolean isShowing) {
        setSystemState(4, !isShowing);
    }

    private void setSystemState(int flags, boolean isOn) {
        synchronized (this.mLock) {
            int oldState = this.mSystemSleepFlags;
            if (isOn) {
                this.mSystemSleepFlags |= flags;
            } else {
                this.mSystemSleepFlags &= ~flags;
            }
            if (oldState != this.mSystemSleepFlags) {
                updateVrModeAllowedLocked();
            }
        }
    }

    private String getStateAsString() {
        StringBuilder sb = new StringBuilder();
        sb.append((this.mSystemSleepFlags & 1) != 0 ? "awake, " : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        sb.append((this.mSystemSleepFlags & 2) != 0 ? "screen_on, " : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        sb.append((this.mSystemSleepFlags & 4) != 0 ? "keyguard_off" : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        return sb.toString();
    }

    /* access modifiers changed from: private */
    public void setUserUnlocked() {
        synchronized (this.mLock) {
            this.mUserUnlocked = true;
            updateVrModeAllowedLocked();
        }
    }

    /* access modifiers changed from: private */
    public void setStandbyEnabled(boolean standby) {
        synchronized (this.mLock) {
            if (!this.mBootsToVr) {
                Slog.e(TAG, "Attempting to set standby mode on a non-standalone device");
                return;
            }
            this.mStandby = standby;
            updateVrModeAllowedLocked();
        }
    }

    public void onEnabledComponentChanged() {
        synchronized (this.mLock) {
            ArraySet<ComponentName> enabledListeners = this.mComponentObserver.getEnabled(ActivityManager.getCurrentUser());
            if (enabledListeners != null) {
                ArraySet<String> enabledPackages = new ArraySet<>();
                Iterator<ComponentName> it = enabledListeners.iterator();
                while (it.hasNext()) {
                    ComponentName n = it.next();
                    if (isDefaultAllowed(n.getPackageName())) {
                        enabledPackages.add(n.getPackageName());
                    }
                }
                this.mNotifAccessManager.update(enabledPackages);
                if (this.mVrModeAllowed) {
                    consumeAndApplyPendingStateLocked(false);
                    if (this.mCurrentVrService != null) {
                        updateCurrentVrServiceLocked(this.mVrModeEnabled, this.mRunning2dInVr, this.mCurrentVrService.getComponent(), this.mCurrentVrService.getUserId(), this.mVrAppProcessId, this.mCurrentVrModeComponent);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void enforceCallerPermissionAnyOf(String... permissions) {
        int length = permissions.length;
        int i = 0;
        while (i < length) {
            if (this.mContext.checkCallingOrSelfPermission(permissions[i]) != 0) {
                i++;
            } else {
                return;
            }
        }
        throw new SecurityException("Caller does not hold at least one of the permissions: " + Arrays.toString(permissions));
    }

    public VrManagerService(Context context) {
        super(context);
        this.mLock = new Object();
        this.mOverlayToken = new Binder();
        this.mVrStateRemoteCallbacks = new RemoteCallbackList<>();
        this.mPersistentVrStateRemoteCallbacks = new RemoteCallbackList<>();
        this.mPreviousCoarseLocationMode = -1;
        this.mPreviousManageOverlayMode = -1;
        this.mLoggingDeque = new ArrayDeque<>(64);
        this.mNotifAccessManager = new NotificationAccessManager();
        this.mSystemSleepFlags = 5;
        this.mHwVMSEx = null;
        this.mEventCallback = new ManagedApplicationService.EventCallback() {
            public void onServiceEvent(ManagedApplicationService.LogEvent event) {
                ComponentName component;
                VrManagerService.this.logEvent(event);
                synchronized (VrManagerService.this.mLock) {
                    component = VrManagerService.this.mCurrentVrService == null ? null : VrManagerService.this.mCurrentVrService.getComponent();
                    if (component != null && component.equals(event.component) && (event.event == 2 || event.event == 3)) {
                        VrManagerService.this.callFocusedActivityChangedLocked();
                    }
                }
                if (!VrManagerService.this.mBootsToVr && event.event == 4) {
                    if (component == null || component.equals(event.component)) {
                        Slog.e(VrManagerService.TAG, "VrListenerSevice has died permanently, leaving system VR mode.");
                        VrManagerService.this.setPersistentVrModeEnabled(false);
                    }
                }
            }
        };
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                boolean z = false;
                switch (msg.what) {
                    case 0:
                        if (msg.arg1 == 1) {
                            z = true;
                        }
                        boolean state = z;
                        int i = VrManagerService.this.mVrStateRemoteCallbacks.beginBroadcast();
                        while (i > 0) {
                            i--;
                            try {
                                VrManagerService.this.mVrStateRemoteCallbacks.getBroadcastItem(i).onVrStateChanged(state);
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
                        if (msg.arg1 == 1) {
                            z = true;
                        }
                        boolean state2 = z;
                        int i2 = VrManagerService.this.mPersistentVrStateRemoteCallbacks.beginBroadcast();
                        while (i2 > 0) {
                            i2--;
                            try {
                                VrManagerService.this.mPersistentVrStateRemoteCallbacks.getBroadcastItem(i2).onPersistentVrStateChanged(state2);
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
        this.mVrManager = new IVrManager.Stub() {
            public void registerListener(IVrStateCallbacks cb) {
                VrManagerService.this.enforceCallerPermissionAnyOf("android.permission.ACCESS_VR_MANAGER", "android.permission.ACCESS_VR_STATE");
                if (cb != null) {
                    VrManagerService.this.addStateCallback(cb);
                    return;
                }
                throw new IllegalArgumentException("Callback binder object is null.");
            }

            public void unregisterListener(IVrStateCallbacks cb) {
                VrManagerService.this.enforceCallerPermissionAnyOf("android.permission.ACCESS_VR_MANAGER", "android.permission.ACCESS_VR_STATE");
                if (cb != null) {
                    VrManagerService.this.removeStateCallback(cb);
                    return;
                }
                throw new IllegalArgumentException("Callback binder object is null.");
            }

            public void registerPersistentVrStateListener(IPersistentVrStateCallbacks cb) {
                VrManagerService.this.enforceCallerPermissionAnyOf("android.permission.ACCESS_VR_MANAGER", "android.permission.ACCESS_VR_STATE");
                if (cb != null) {
                    VrManagerService.this.addPersistentStateCallback(cb);
                    return;
                }
                throw new IllegalArgumentException("Callback binder object is null.");
            }

            public void unregisterPersistentVrStateListener(IPersistentVrStateCallbacks cb) {
                VrManagerService.this.enforceCallerPermissionAnyOf("android.permission.ACCESS_VR_MANAGER", "android.permission.ACCESS_VR_STATE");
                if (cb != null) {
                    VrManagerService.this.removePersistentStateCallback(cb);
                    return;
                }
                throw new IllegalArgumentException("Callback binder object is null.");
            }

            public boolean getVrModeState() {
                VrManagerService.this.enforceCallerPermissionAnyOf("android.permission.ACCESS_VR_MANAGER", "android.permission.ACCESS_VR_STATE");
                return VrManagerService.this.getVrMode();
            }

            public boolean getPersistentVrModeEnabled() {
                VrManagerService.this.enforceCallerPermissionAnyOf("android.permission.ACCESS_VR_MANAGER", "android.permission.ACCESS_VR_STATE");
                return VrManagerService.this.getPersistentVrMode();
            }

            public void setPersistentVrModeEnabled(boolean enabled) {
                VrManagerService.this.enforceCallerPermissionAnyOf("android.permission.RESTRICTED_VR_ACCESS");
                VrManagerService.this.setPersistentVrModeEnabled(enabled);
            }

            public void setVr2dDisplayProperties(Vr2dDisplayProperties vr2dDisplayProp) {
                VrManagerService.this.enforceCallerPermissionAnyOf("android.permission.RESTRICTED_VR_ACCESS");
                VrManagerService.this.setVr2dDisplayProperties(vr2dDisplayProp);
            }

            public int getVr2dDisplayId() {
                return VrManagerService.this.getVr2dDisplayId();
            }

            public void setAndBindCompositor(String componentName) {
                VrManagerService.this.enforceCallerPermissionAnyOf("android.permission.RESTRICTED_VR_ACCESS");
                VrManagerService.this.setAndBindCompositor(componentName == null ? null : ComponentName.unflattenFromString(componentName));
            }

            public void setStandbyEnabled(boolean standby) {
                VrManagerService.this.enforceCallerPermissionAnyOf("android.permission.ACCESS_VR_MANAGER");
                VrManagerService.this.setStandbyEnabled(standby);
            }

            public void setVrInputMethod(ComponentName componentName) {
                VrManagerService.this.enforceCallerPermissionAnyOf("android.permission.RESTRICTED_VR_ACCESS");
                ((InputMethodManagerInternal) LocalServices.getService(InputMethodManagerInternal.class)).startVrInputMethodNoCheck(componentName);
            }

            /* access modifiers changed from: protected */
            public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
                if (DumpUtils.checkDumpPermission(VrManagerService.this.mContext, VrManagerService.TAG, pw)) {
                    pw.println("********* Dump of VrManagerService *********");
                    StringBuilder sb = new StringBuilder();
                    sb.append("VR mode is currently: ");
                    sb.append(VrManagerService.this.mVrModeAllowed ? "allowed" : "disallowed");
                    pw.println(sb.toString());
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("Persistent VR mode is currently: ");
                    sb2.append(VrManagerService.this.mPersistentVrModeEnabled ? "enabled" : "disabled");
                    pw.println(sb2.toString());
                    StringBuilder sb3 = new StringBuilder();
                    sb3.append("Currently bound VR listener service: ");
                    sb3.append(VrManagerService.this.mCurrentVrService == null ? "None" : VrManagerService.this.mCurrentVrService.getComponent().flattenToString());
                    pw.println(sb3.toString());
                    StringBuilder sb4 = new StringBuilder();
                    sb4.append("Currently bound VR compositor service: ");
                    sb4.append(VrManagerService.this.mCurrentVrCompositorService == null ? "None" : VrManagerService.this.mCurrentVrCompositorService.getComponent().flattenToString());
                    pw.println(sb4.toString());
                    pw.println("Previous state transitions:\n");
                    VrManagerService.this.dumpStateTransitions(pw);
                    pw.println("\n\nRemote Callbacks:");
                    int i = VrManagerService.this.mVrStateRemoteCallbacks.beginBroadcast();
                    while (true) {
                        int i2 = i - 1;
                        if (i <= 0) {
                            break;
                        }
                        pw.print("  ");
                        pw.print(VrManagerService.this.mVrStateRemoteCallbacks.getBroadcastItem(i2));
                        if (i2 > 0) {
                            pw.println(",");
                        }
                        i = i2;
                    }
                    VrManagerService.this.mVrStateRemoteCallbacks.finishBroadcast();
                    pw.println("\n\nPersistent Vr State Remote Callbacks:");
                    int i3 = VrManagerService.this.mPersistentVrStateRemoteCallbacks.beginBroadcast();
                    while (true) {
                        int i4 = i3 - 1;
                        if (i3 <= 0) {
                            break;
                        }
                        pw.print("  ");
                        pw.print(VrManagerService.this.mPersistentVrStateRemoteCallbacks.getBroadcastItem(i4));
                        if (i4 > 0) {
                            pw.println(",");
                        }
                        i3 = i4;
                    }
                    VrManagerService.this.mPersistentVrStateRemoteCallbacks.finishBroadcast();
                    pw.println("\n");
                    pw.println("Installed VrListenerService components:");
                    int userId = VrManagerService.this.mCurrentVrModeUser;
                    ArraySet<ComponentName> installed = VrManagerService.this.mComponentObserver.getInstalled(userId);
                    if (installed == null || installed.size() == 0) {
                        pw.println("None");
                    } else {
                        Iterator<ComponentName> it = installed.iterator();
                        while (it.hasNext()) {
                            pw.print("  ");
                            pw.println(it.next().flattenToString());
                        }
                    }
                    pw.println("Enabled VrListenerService components:");
                    ArraySet<ComponentName> enabled = VrManagerService.this.mComponentObserver.getEnabled(userId);
                    if (enabled == null || enabled.size() == 0) {
                        pw.println("None");
                    } else {
                        Iterator<ComponentName> it2 = enabled.iterator();
                        while (it2.hasNext()) {
                            pw.print("  ");
                            pw.println(it2.next().flattenToString());
                        }
                    }
                    pw.println("\n");
                    pw.println("********* End of VrManagerService Dump *********");
                }
            }
        };
        this.mHwVMSEx = HwServiceExFactory.getHwVrManagerServiceEx();
    }

    public void onStart() {
        synchronized (this.mLock) {
            initializeNative();
            this.mContext = getContext();
        }
        boolean z = false;
        this.mBootsToVr = SystemProperties.getBoolean("ro.boot.vr", false);
        if (this.mBootsToVr && SystemProperties.getBoolean("persist.vr.use_standby_to_exit_vr_mode", true)) {
            z = true;
        }
        this.mUseStandbyToExitVrMode = z;
        publishLocalService(VrManagerInternal.class, new LocalService());
        publishBinderService("vrmanager", this.mVrManager.asBinder());
    }

    public void onBootPhase(int phase) {
        if (phase == 500) {
            ((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).registerScreenObserver(this);
            this.mNotificationManager = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
            synchronized (this.mLock) {
                Looper looper = Looper.getMainLooper();
                Handler handler = new Handler(looper);
                ArrayList arrayList = new ArrayList();
                arrayList.add(this);
                this.mComponentObserver = EnabledComponentsObserver.build(this.mContext, handler, "enabled_vr_listeners", looper, "android.permission.BIND_VR_LISTENER_SERVICE", "android.service.vr.VrListenerService", this.mLock, arrayList);
                this.mComponentObserver.rebuildAll();
            }
            ArraySet<ComponentName> defaultVrComponents = SystemConfig.getInstance().getDefaultVrComponents();
            if (defaultVrComponents.size() > 0) {
                this.mDefaultVrService = defaultVrComponents.valueAt(0);
            } else {
                Slog.i(TAG, "No default vr listener service found.");
            }
            this.mVr2dDisplay = new Vr2dDisplay((DisplayManager) getContext().getSystemService("display"), (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class), (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class), this.mVrManager);
            this.mVr2dDisplay.init(getContext(), this.mBootsToVr);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.USER_UNLOCKED");
            getContext().registerReceiver(new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if ("android.intent.action.USER_UNLOCKED".equals(intent.getAction())) {
                        VrManagerService.this.setUserUnlocked();
                    }
                }
            }, intentFilter);
        }
    }

    public void onStartUser(int userHandle) {
        synchronized (this.mLock) {
            this.mComponentObserver.onUsersChanged();
        }
    }

    public void onSwitchUser(int userHandle) {
        FgThread.getHandler().post(new Runnable() {
            public final void run() {
                VrManagerService.lambda$onSwitchUser$0(VrManagerService.this);
            }
        });
    }

    public static /* synthetic */ void lambda$onSwitchUser$0(VrManagerService vrManagerService) {
        synchronized (vrManagerService.mLock) {
            vrManagerService.mComponentObserver.onUsersChanged();
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
        String[] exemptions;
        AppOpsManager appOpsManager = (AppOpsManager) getContext().getSystemService(AppOpsManager.class);
        if (oldUserId != newUserId) {
            appOpsManager.setUserRestrictionForUser(24, false, this.mOverlayToken, null, oldUserId);
        }
        if (exemptedPackage == null) {
            exemptions = new String[0];
        } else {
            exemptions = new String[]{exemptedPackage};
        }
        appOpsManager.setUserRestrictionForUser(24, this.mVrModeEnabled, this.mOverlayToken, exemptions, newUserId);
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

    private boolean updateCurrentVrServiceLocked(boolean enabled, boolean running2dInVr, ComponentName component, int userId, int processId, ComponentName calling) {
        String oldVrServicePackage;
        int oldUserId;
        boolean nothingChanged;
        boolean sendUpdatedCaller;
        boolean sendUpdatedCaller2;
        boolean sendUpdatedCaller3;
        boolean z = running2dInVr;
        ComponentName componentName = component;
        int i = userId;
        ComponentName componentName2 = calling;
        boolean sendUpdatedCaller4 = false;
        long identity = Binder.clearCallingIdentity();
        boolean goingIntoVrMode = false;
        boolean validUserComponent = this.mComponentObserver.isValid(componentName, i) == 0;
        if (validUserComponent && enabled) {
            goingIntoVrMode = true;
        }
        if (this.mVrModeEnabled || goingIntoVrMode) {
            try {
                if (this.mCurrentVrService != null) {
                    try {
                        oldVrServicePackage = this.mCurrentVrService.getComponent().getPackageName();
                    } catch (Throwable th) {
                        th = th;
                        int i2 = processId;
                        Binder.restoreCallingIdentity(identity);
                        throw th;
                    }
                } else {
                    oldVrServicePackage = null;
                }
                oldUserId = this.mCurrentVrModeUser;
                changeVrModeLocked(goingIntoVrMode);
                nothingChanged = false;
                if (!goingIntoVrMode) {
                    try {
                        if (this.mCurrentVrService != null) {
                            StringBuilder sb = new StringBuilder();
                            sendUpdatedCaller = false;
                            sb.append("Leaving VR mode, disconnecting ");
                            sb.append(this.mCurrentVrService.getComponent());
                            sb.append(" for user ");
                            sb.append(this.mCurrentVrService.getUserId());
                            Slog.i(TAG, sb.toString());
                            this.mCurrentVrService.disconnect();
                            updateCompositorServiceLocked(-10000, null);
                            this.mCurrentVrService = null;
                        } else {
                            sendUpdatedCaller = false;
                            nothingChanged = true;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        int i3 = processId;
                        Binder.restoreCallingIdentity(identity);
                        throw th;
                    }
                } else {
                    sendUpdatedCaller = false;
                    if (this.mCurrentVrService == null) {
                        createAndConnectService(componentName, i);
                        sendUpdatedCaller3 = true;
                    } else if (this.mCurrentVrService.disconnectIfNotMatching(componentName, i)) {
                        Slog.i(TAG, "VR mode component changed to " + componentName + ", disconnecting " + this.mCurrentVrService.getComponent() + " for user " + this.mCurrentVrService.getUserId());
                        updateCompositorServiceLocked(-10000, null);
                        createAndConnectService(componentName, i);
                        sendUpdatedCaller3 = true;
                    } else {
                        nothingChanged = true;
                    }
                    sendUpdatedCaller = sendUpdatedCaller3;
                }
                if (((componentName2 != null || this.mPersistentVrModeEnabled) && !Objects.equals(componentName2, this.mCurrentVrModeComponent)) || this.mRunning2dInVr != z) {
                    sendUpdatedCaller2 = true;
                } else {
                    sendUpdatedCaller2 = sendUpdatedCaller;
                }
            } catch (Throwable th3) {
                th = th3;
                int i4 = processId;
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
            try {
                this.mCurrentVrModeComponent = componentName2;
                this.mRunning2dInVr = z;
            } catch (Throwable th4) {
                th = th4;
                int i5 = processId;
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
            try {
                this.mVrAppProcessId = processId;
                if (this.mCurrentVrModeUser != i) {
                    this.mCurrentVrModeUser = i;
                    sendUpdatedCaller4 = true;
                } else {
                    sendUpdatedCaller4 = sendUpdatedCaller2;
                }
                try {
                    String newVrServicePackage = this.mCurrentVrService != null ? this.mCurrentVrService.getComponent().getPackageName() : null;
                    int newUserId = this.mCurrentVrModeUser;
                    updateDependentAppOpsLocked(newVrServicePackage, newUserId, oldVrServicePackage, oldUserId);
                    int i6 = newUserId;
                    if (this.mCurrentVrService != null && sendUpdatedCaller4) {
                        callFocusedActivityChangedLocked();
                    }
                    if (!nothingChanged) {
                        logStateLocked();
                    }
                    Binder.restoreCallingIdentity(identity);
                    return validUserComponent;
                } catch (Throwable th5) {
                    th = th5;
                    Binder.restoreCallingIdentity(identity);
                    throw th;
                }
            } catch (Throwable th6) {
                th = th6;
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        } else {
            Binder.restoreCallingIdentity(identity);
            return validUserComponent;
        }
    }

    /* access modifiers changed from: private */
    public void callFocusedActivityChangedLocked() {
        final ComponentName c = this.mCurrentVrModeComponent;
        final boolean b = this.mRunning2dInVr;
        final int pid = this.mVrAppProcessId;
        this.mCurrentVrService.sendEvent(new ManagedApplicationService.PendingEvent() {
            public void runEvent(IInterface service) throws RemoteException {
                ((IVrListener) service).focusedActivityChanged(c, b, pid);
            }
        });
    }

    private boolean isDefaultAllowed(String packageName) {
        ApplicationInfo info = null;
        try {
            info = this.mContext.getPackageManager().getApplicationInfo(packageName, 128);
        } catch (PackageManager.NameNotFoundException e) {
        }
        if (info == null || (!info.isSystemApp() && !info.isUpdatedSystemApp())) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void grantNotificationPolicyAccess(String pkg) {
        ((NotificationManager) this.mContext.getSystemService(NotificationManager.class)).setNotificationPolicyAccessGranted(pkg, true);
    }

    /* access modifiers changed from: private */
    public void revokeNotificationPolicyAccess(String pkg) {
        NotificationManager nm = (NotificationManager) this.mContext.getSystemService(NotificationManager.class);
        nm.removeAutomaticZenRules(pkg);
        nm.setNotificationPolicyAccessGranted(pkg, false);
    }

    /* access modifiers changed from: private */
    public void grantNotificationListenerAccess(String pkg, int userId) {
        NotificationManager nm = (NotificationManager) this.mContext.getSystemService(NotificationManager.class);
        Iterator<ComponentName> it = EnabledComponentsObserver.loadComponentNames(this.mContext.getPackageManager(), userId, "android.service.notification.NotificationListenerService", "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE").iterator();
        while (it.hasNext()) {
            ComponentName c = it.next();
            if (Objects.equals(c.getPackageName(), pkg)) {
                nm.setNotificationListenerAccessGrantedForUser(c, userId, true);
            }
        }
    }

    /* access modifiers changed from: private */
    public void revokeNotificationListenerAccess(String pkg, int userId) {
        NotificationManager nm = (NotificationManager) this.mContext.getSystemService(NotificationManager.class);
        for (ComponentName component : nm.getEnabledNotificationListeners(userId)) {
            if (component != null && component.getPackageName().equals(pkg)) {
                nm.setNotificationListenerAccessGrantedForUser(component, userId, false);
            }
        }
    }

    /* access modifiers changed from: private */
    public void grantCoarseLocationPermissionIfNeeded(String pkg, int userId) {
        if (!isPermissionUserUpdated("android.permission.ACCESS_COARSE_LOCATION", pkg, userId)) {
            try {
                this.mContext.getPackageManager().grantRuntimePermission(pkg, "android.permission.ACCESS_COARSE_LOCATION", new UserHandle(userId));
            } catch (IllegalArgumentException e) {
                Slog.w(TAG, "Could not grant coarse location permission, package " + pkg + " was removed.");
            }
        }
    }

    /* access modifiers changed from: private */
    public void revokeCoarseLocationPermissionIfNeeded(String pkg, int userId) {
        if (!isPermissionUserUpdated("android.permission.ACCESS_COARSE_LOCATION", pkg, userId)) {
            try {
                this.mContext.getPackageManager().revokeRuntimePermission(pkg, "android.permission.ACCESS_COARSE_LOCATION", new UserHandle(userId));
            } catch (IllegalArgumentException e) {
                Slog.w(TAG, "Could not revoke coarse location permission, package " + pkg + " was removed.");
            }
        }
    }

    private boolean isPermissionUserUpdated(String permission, String pkg, int userId) {
        return (this.mContext.getPackageManager().getPermissionFlags(permission, pkg, new UserHandle(userId)) & 3) != 0;
    }

    private ArraySet<String> getNotificationListeners(ContentResolver resolver, int userId) {
        String flat = Settings.Secure.getStringForUser(resolver, "enabled_notification_listeners", userId);
        ArraySet<String> current = new ArraySet<>();
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
            return BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        }
        StringBuilder b = new StringBuilder();
        boolean start = true;
        for (String s : c) {
            if (!BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS.equals(s)) {
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
        this.mCurrentVrService = createVrListenerService(component, userId);
        this.mCurrentVrService.connect();
        Slog.i(TAG, "Connecting " + component + " for user " + userId);
    }

    private void changeVrModeLocked(boolean enabled) {
        if (this.mVrModeEnabled != enabled) {
            this.mVrModeEnabled = enabled;
            StringBuilder sb = new StringBuilder();
            sb.append("VR mode ");
            sb.append(this.mVrModeEnabled ? "enabled" : "disabled");
            Slog.i(TAG, sb.toString());
            setVrModeNative(this.mVrModeEnabled);
            if (this.mHwVMSEx != null) {
                this.mHwVMSEx.setHwEnviroment(enabled);
            }
            onVrModeChangedLocked();
        }
    }

    private void onVrModeChangedLocked() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(0, this.mVrModeEnabled ? 1 : 0, 0));
    }

    private ManagedApplicationService createVrListenerService(ComponentName component, int userId) {
        int retryType;
        if (this.mBootsToVr) {
            retryType = 1;
        } else {
            retryType = 2;
        }
        return ManagedApplicationService.build(this.mContext, component, userId, 17041348, "android.settings.VR_LISTENER_SETTINGS", sBinderChecker, true, retryType, this.mHandler, this.mEventCallback);
    }

    private ManagedApplicationService createVrCompositorService(ComponentName component, int userId) {
        int retryType;
        if (this.mBootsToVr) {
            retryType = 1;
        } else {
            retryType = 3;
        }
        return ManagedApplicationService.build(this.mContext, component, userId, 0, null, null, true, retryType, this.mHandler, this.mEventCallback);
    }

    /* access modifiers changed from: private */
    public void consumeAndApplyPendingStateLocked() {
        consumeAndApplyPendingStateLocked(true);
    }

    private void consumeAndApplyPendingStateLocked(boolean disconnectIfNoPendingState) {
        if (this.mPendingState != null) {
            updateCurrentVrServiceLocked(this.mPendingState.enabled, this.mPendingState.running2dInVr, this.mPendingState.targetPackageName, this.mPendingState.userId, this.mPendingState.processId, this.mPendingState.callingPackage);
            this.mPendingState = null;
        } else if (disconnectIfNoPendingState) {
            updateCurrentVrServiceLocked(false, false, null, 0, -1, null);
        }
    }

    private void logStateLocked() {
        ComponentName currentBoundService;
        if (this.mCurrentVrService == null) {
            currentBoundService = null;
        } else {
            currentBoundService = this.mCurrentVrService.getComponent();
        }
        VrState vrState = new VrState(this.mVrModeEnabled, this.mRunning2dInVr, currentBoundService, this.mCurrentVrModeUser, this.mVrAppProcessId, this.mCurrentVrModeComponent, this.mWasDefaultGranted);
        logEvent(vrState);
    }

    /* access modifiers changed from: private */
    public void logEvent(ManagedApplicationService.LogFormattable event) {
        synchronized (this.mLoggingDeque) {
            if (this.mLoggingDeque.size() == 64) {
                this.mLoggingDeque.removeFirst();
                this.mLogLimitHit = true;
            }
            this.mLoggingDeque.add(event);
        }
    }

    /* access modifiers changed from: private */
    public void dumpStateTransitions(PrintWriter pw) {
        SimpleDateFormat d = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
        synchronized (this.mLoggingDeque) {
            if (this.mLoggingDeque.size() == 0) {
                pw.print("  ");
                pw.println("None");
            }
            if (this.mLogLimitHit) {
                pw.println("...");
            }
            Iterator<ManagedApplicationService.LogFormattable> it = this.mLoggingDeque.iterator();
            while (it.hasNext()) {
                pw.println(it.next().toLogString(d));
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0020 A[Catch:{ all -> 0x0010 }] */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0023 A[Catch:{ all -> 0x0010 }] */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x003d A[Catch:{ all -> 0x0010 }] */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0041 A[Catch:{ all -> 0x0010 }] */
    public void setVrMode(boolean enabled, ComponentName targetPackageName, int userId, int processId, ComponentName callingPackage) {
        boolean z;
        ComponentName targetListener;
        synchronized (this.mLock) {
            boolean running2dInVr = false;
            if (!enabled) {
                try {
                    if (!this.mPersistentVrModeEnabled) {
                        z = false;
                        boolean targetEnabledState = z;
                        if (!enabled && this.mPersistentVrModeEnabled) {
                            running2dInVr = true;
                        }
                        if (!running2dInVr) {
                            targetListener = this.mDefaultVrService;
                        } else {
                            targetListener = targetPackageName;
                        }
                        ComponentName targetListener2 = targetListener;
                        VrState vrState = new VrState(targetEnabledState, running2dInVr, targetListener2, userId, processId, callingPackage);
                        VrState pending = vrState;
                        if (this.mVrModeAllowed) {
                            this.mPendingState = pending;
                            return;
                        } else if (targetEnabledState || this.mCurrentVrService == null) {
                            this.mHandler.removeMessages(1);
                            this.mPendingState = null;
                            if (targetListener2 != null) {
                                updateCurrentVrServiceLocked(targetEnabledState, running2dInVr, targetListener2, userId, processId, callingPackage);
                                return;
                            }
                            return;
                        } else {
                            if (this.mPendingState == null) {
                                this.mHandler.sendEmptyMessageDelayed(1, 300);
                            }
                            this.mPendingState = pending;
                            return;
                        }
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
            z = true;
            boolean targetEnabledState2 = z;
            running2dInVr = true;
            if (!running2dInVr) {
            }
            ComponentName targetListener22 = targetListener;
            VrState vrState2 = new VrState(targetEnabledState2, running2dInVr, targetListener22, userId, processId, callingPackage);
            VrState pending2 = vrState2;
            if (this.mVrModeAllowed) {
            }
        }
    }

    /* access modifiers changed from: private */
    public void setPersistentVrModeEnabled(boolean enabled) {
        synchronized (this.mLock) {
            setPersistentModeAndNotifyListenersLocked(enabled);
            if (!enabled) {
                setVrMode(false, null, 0, -1, null);
            }
        }
    }

    public void setVr2dDisplayProperties(Vr2dDisplayProperties compatDisplayProp) {
        long token = Binder.clearCallingIdentity();
        try {
            if (this.mVr2dDisplay != null) {
                this.mVr2dDisplay.setVirtualDisplayProperties(compatDisplayProp);
                return;
            }
            Binder.restoreCallingIdentity(token);
            Slog.w(TAG, "Vr2dDisplay is null!");
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    /* access modifiers changed from: private */
    public int getVr2dDisplayId() {
        if (this.mVr2dDisplay != null) {
            return this.mVr2dDisplay.getVirtualDisplayId();
        }
        Slog.w(TAG, "Vr2dDisplay is null!");
        return -1;
    }

    /* access modifiers changed from: private */
    public void setAndBindCompositor(ComponentName componentName) {
        int userId = UserHandle.getCallingUserId();
        long token = Binder.clearCallingIdentity();
        try {
            synchronized (this.mLock) {
                updateCompositorServiceLocked(userId, componentName);
            }
            Binder.restoreCallingIdentity(token);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    private void updateCompositorServiceLocked(int userId, ComponentName componentName) {
        if (this.mCurrentVrCompositorService != null && this.mCurrentVrCompositorService.disconnectIfNotMatching(componentName, userId)) {
            Slog.i(TAG, "Disconnecting compositor service: " + this.mCurrentVrCompositorService.getComponent());
            this.mCurrentVrCompositorService = null;
        }
        if (componentName != null && this.mCurrentVrCompositorService == null) {
            Slog.i(TAG, "Connecting compositor service: " + componentName);
            this.mCurrentVrCompositorService = createVrCompositorService(componentName, userId);
            this.mCurrentVrCompositorService.connect();
        }
    }

    private void setPersistentModeAndNotifyListenersLocked(boolean enabled) {
        if (this.mPersistentVrModeEnabled != enabled) {
            StringBuilder sb = new StringBuilder();
            sb.append("Persistent VR mode ");
            sb.append(enabled ? "enabled" : "disabled");
            String eventName = sb.toString();
            Slog.i(TAG, eventName);
            logEvent(new SettingEvent(eventName));
            this.mPersistentVrModeEnabled = enabled;
            this.mHandler.sendMessage(this.mHandler.obtainMessage(2, this.mPersistentVrModeEnabled ? 1 : 0, 0));
        }
    }

    /* access modifiers changed from: private */
    public int hasVrPackage(ComponentName targetPackageName, int userId) {
        int isValid;
        synchronized (this.mLock) {
            isValid = this.mComponentObserver.isValid(targetPackageName, userId);
        }
        return isValid;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0025, code lost:
        return r2;
     */
    public boolean isCurrentVrListener(String packageName, int userId) {
        synchronized (this.mLock) {
            boolean z = false;
            if (this.mCurrentVrService == null) {
                return false;
            }
            if (this.mCurrentVrService.getComponent().getPackageName().equals(packageName) && userId == this.mCurrentVrService.getUserId()) {
                z = true;
            }
        }
    }

    /* access modifiers changed from: private */
    public void addStateCallback(IVrStateCallbacks cb) {
        this.mVrStateRemoteCallbacks.register(cb);
    }

    /* access modifiers changed from: private */
    public void removeStateCallback(IVrStateCallbacks cb) {
        this.mVrStateRemoteCallbacks.unregister(cb);
    }

    /* access modifiers changed from: private */
    public void addPersistentStateCallback(IPersistentVrStateCallbacks cb) {
        this.mPersistentVrStateRemoteCallbacks.register(cb);
    }

    /* access modifiers changed from: private */
    public void removePersistentStateCallback(IPersistentVrStateCallbacks cb) {
        this.mPersistentVrStateRemoteCallbacks.unregister(cb);
    }

    /* access modifiers changed from: private */
    public boolean getVrMode() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mVrModeEnabled;
        }
        return z;
    }

    /* access modifiers changed from: private */
    public boolean getPersistentVrMode() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mPersistentVrModeEnabled;
        }
        return z;
    }
}
