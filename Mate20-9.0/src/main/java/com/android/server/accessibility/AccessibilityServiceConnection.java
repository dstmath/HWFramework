package com.android.server.accessibility;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.IAccessibilityServiceClient;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ParceledListSlice;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Slog;
import com.android.internal.util.function.pooled.PooledLambda;
import com.android.server.accessibility.AbstractAccessibilityServiceConnection;
import com.android.server.accessibility.AccessibilityManagerService;
import com.android.server.pm.DumpState;
import com.android.server.wm.WindowManagerInternal;
import java.lang.ref.WeakReference;
import java.util.Set;

class AccessibilityServiceConnection extends AbstractAccessibilityServiceConnection {
    private static final String LOG_TAG = "AccessibilityServiceConnection";
    final Intent mIntent = new Intent().setComponent(this.mComponentName);
    private final Handler mMainHandler;
    final WeakReference<AccessibilityManagerService.UserState> mUserStateWeakReference;
    private boolean mWasConnectedAndDied;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public AccessibilityServiceConnection(AccessibilityManagerService.UserState userState, Context context, ComponentName componentName, AccessibilityServiceInfo accessibilityServiceInfo, int id, Handler mainHandler, Object lock, AccessibilityManagerService.SecurityPolicy securityPolicy, AbstractAccessibilityServiceConnection.SystemSupport systemSupport, WindowManagerInternal windowManagerInternal, GlobalActionPerformer globalActionPerfomer) {
        super(context, componentName, accessibilityServiceInfo, id, mainHandler, lock, securityPolicy, systemSupport, windowManagerInternal, globalActionPerfomer);
        this.mUserStateWeakReference = new WeakReference<>(userState);
        this.mMainHandler = mainHandler;
        this.mIntent.putExtra("android.intent.extra.client_label", 17039526);
        long identity = Binder.clearCallingIdentity();
        try {
            this.mIntent.putExtra("android.intent.extra.client_intent", this.mSystemSupport.getPendingIntentActivity(this.mContext, 0, new Intent("android.settings.ACCESSIBILITY_SETTINGS"), 0));
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void bindLocked() {
        AccessibilityManagerService.UserState userState = (AccessibilityManagerService.UserState) this.mUserStateWeakReference.get();
        if (userState != null) {
            long identity = Binder.clearCallingIdentity();
            int flags = 33554433;
            try {
                if (userState.mBindInstantServiceAllowed) {
                    flags = 33554433 | DumpState.DUMP_CHANGES;
                }
                if (this.mService == null && this.mContext.bindServiceAsUser(this.mIntent, this, flags, new UserHandle(userState.mUserId))) {
                    userState.getBindingServicesLocked().add(this.mComponentName);
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    public void unbindLocked() {
        this.mContext.unbindService(this);
        AccessibilityManagerService.UserState userState = (AccessibilityManagerService.UserState) this.mUserStateWeakReference.get();
        if (userState != null) {
            userState.removeServiceLocked(this);
            resetLocked();
        }
    }

    public boolean canRetrieveInteractiveWindowsLocked() {
        return this.mSecurityPolicy.canRetrieveWindowContentLocked(this) && this.mRetrieveInteractiveWindows;
    }

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0039, code lost:
        return;
     */
    public void disableSelf() {
        synchronized (this.mLock) {
            AccessibilityManagerService.UserState userState = (AccessibilityManagerService.UserState) this.mUserStateWeakReference.get();
            if (userState != null) {
                if (userState.mEnabledServices.remove(this.mComponentName)) {
                    long identity = Binder.clearCallingIdentity();
                    try {
                        this.mSystemSupport.persistComponentNamesToSettingLocked("enabled_accessibility_services", userState.mEnabledServices, userState.mUserId);
                        Binder.restoreCallingIdentity(identity);
                        this.mSystemSupport.onClientChange(false);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(identity);
                        throw th;
                    }
                }
            }
        }
    }

    public void onServiceConnected(ComponentName componentName, IBinder service) {
        synchronized (this.mLock) {
            if (this.mService != service) {
                if (this.mService != null) {
                    this.mService.unlinkToDeath(this, 0);
                }
                this.mService = service;
                try {
                    this.mService.linkToDeath(this, 0);
                } catch (RemoteException e) {
                    Slog.e(LOG_TAG, "Failed registering death link");
                    binderDied();
                    return;
                }
            }
            this.mServiceInterface = IAccessibilityServiceClient.Stub.asInterface(service);
            AccessibilityManagerService.UserState userState = (AccessibilityManagerService.UserState) this.mUserStateWeakReference.get();
            if (userState != null) {
                userState.addServiceLocked(this);
                this.mSystemSupport.onClientChange(false);
                this.mMainHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$AccessibilityServiceConnection$ASP9bmSvpeD7ZE_uJ8sm9hCwiU.INSTANCE, this));
            }
        }
    }

    public AccessibilityServiceInfo getServiceInfo() {
        this.mAccessibilityServiceInfo.crashed = this.mWasConnectedAndDied;
        return this.mAccessibilityServiceInfo;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x002c, code lost:
        if (r0 != null) goto L_0x0032;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002e, code lost:
        binderDied();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0031, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
        r0.init(r5, r5.mId, r5.mOverlayWindowToken);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x003a, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x003b, code lost:
        android.util.Slog.w(LOG_TAG, "Error while setting connection for service: " + r0, r1);
        binderDied();
     */
    public void initializeService() {
        IAccessibilityServiceClient serviceInterface = null;
        synchronized (this.mLock) {
            AccessibilityManagerService.UserState userState = (AccessibilityManagerService.UserState) this.mUserStateWeakReference.get();
            if (userState != null) {
                Set<ComponentName> bindingServices = userState.getBindingServicesLocked();
                if (bindingServices.contains(this.mComponentName) || this.mWasConnectedAndDied) {
                    bindingServices.remove(this.mComponentName);
                    this.mWasConnectedAndDied = false;
                    serviceInterface = this.mServiceInterface;
                }
            }
        }
    }

    public void onServiceDisconnected(ComponentName componentName) {
        binderDied();
    }

    /* access modifiers changed from: protected */
    public boolean isCalledForCurrentUserLocked() {
        return this.mSecurityPolicy.resolveCallingUserIdEnforcingPermissionsLocked(-2) == this.mSystemSupport.getCurrentUserIdLocked();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0017, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0018, code lost:
        r1 = android.os.Binder.clearCallingIdentity();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001c, code lost:
        if (r7 != 0) goto L_0x0020;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001e, code lost:
        r3 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
        r3 = r6.mComponentName;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0022, code lost:
        r0.mServiceChangingSoftKeyboardMode = r3;
        android.provider.Settings.Secure.putIntForUser(r6.mContext.getContentResolver(), "accessibility_soft_keyboard_mode", r7, r0.mUserId);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0031, code lost:
        android.os.Binder.restoreCallingIdentity(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0036, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0037, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0038, code lost:
        android.os.Binder.restoreCallingIdentity(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x003b, code lost:
        throw r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000d, code lost:
        r0 = (com.android.server.accessibility.AccessibilityManagerService.UserState) r6.mUserStateWeakReference.get();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0015, code lost:
        if (r0 != null) goto L_0x0018;
     */
    public boolean setSoftKeyboardShowMode(int showMode) {
        synchronized (this.mLock) {
            if (!isCalledForCurrentUserLocked()) {
                return false;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001f, code lost:
        return r2;
     */
    public boolean isAccessibilityButtonAvailable() {
        synchronized (this.mLock) {
            boolean z = false;
            if (!isCalledForCurrentUserLocked()) {
                return false;
            }
            AccessibilityManagerService.UserState userState = (AccessibilityManagerService.UserState) this.mUserStateWeakReference.get();
            if (userState != null && isAccessibilityButtonAvailableLocked(userState)) {
                z = true;
            }
        }
    }

    public void binderDied() {
        synchronized (this.mLock) {
            if (isConnectedLocked()) {
                this.mWasConnectedAndDied = true;
                resetLocked();
                if (this.mId == this.mSystemSupport.getMagnificationController().getIdOfLastServiceToMagnify()) {
                    this.mSystemSupport.getMagnificationController().resetIfNeeded(true);
                }
                Slog.i(LOG_TAG, "volume_debug binder Died, set Filter to null.");
                this.mWindowManagerService.setInputFilter(null);
                this.mSystemSupport.onClientChange(false);
            }
        }
    }

    public boolean isAccessibilityButtonAvailableLocked(AccessibilityManagerService.UserState userState) {
        if (!this.mRequestAccessibilityButton || !this.mSystemSupport.isAccessibilityButtonShown()) {
            return false;
        }
        if (userState.mIsNavBarMagnificationEnabled && userState.mIsNavBarMagnificationAssignedToAccessibilityButton) {
            return false;
        }
        int requestingServices = 0;
        for (int i = userState.mBoundServices.size() - 1; i >= 0; i--) {
            if (userState.mBoundServices.get(i).mRequestAccessibilityButton) {
                requestingServices++;
            }
        }
        if (requestingServices == 1 || userState.mServiceAssignedToAccessibilityButton == null) {
            return true;
        }
        return this.mComponentName.equals(userState.mServiceAssignedToAccessibilityButton);
    }

    public boolean isCapturingFingerprintGestures() {
        return this.mServiceInterface != null && this.mSecurityPolicy.canCaptureFingerprintGestures(this) && this.mCaptureFingerprintGestures;
    }

    public void onFingerprintGestureDetectionActiveChanged(boolean active) {
        IAccessibilityServiceClient serviceInterface;
        if (isCapturingFingerprintGestures()) {
            synchronized (this.mLock) {
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
        IAccessibilityServiceClient serviceInterface;
        if (isCapturingFingerprintGestures()) {
            synchronized (this.mLock) {
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

    public void sendGesture(int sequence, ParceledListSlice gestureSteps) {
        synchronized (this.mLock) {
            if (this.mSecurityPolicy.canPerformGestures(this)) {
                MotionEventInjector motionEventInjector = this.mSystemSupport.getMotionEventInjectorLocked();
                if (motionEventInjector != null) {
                    motionEventInjector.injectEvents(gestureSteps.getList(), this.mServiceInterface, sequence);
                } else {
                    try {
                        this.mServiceInterface.onPerformGestureResult(sequence, false);
                    } catch (RemoteException re) {
                        Slog.e(LOG_TAG, "Error sending motion event injection failure to " + this.mServiceInterface, re);
                    }
                }
            }
        }
    }
}
