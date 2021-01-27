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
import android.view.IInputFilter;
import com.android.internal.util.function.pooled.PooledLambda;
import com.android.server.accessibility.AbstractAccessibilityServiceConnection;
import com.android.server.accessibility.AccessibilityManagerService;
import com.android.server.pm.DumpState;
import com.android.server.wm.WindowManagerInternal;
import java.lang.ref.WeakReference;
import java.util.NoSuchElementException;
import java.util.Set;

/* access modifiers changed from: package-private */
public class AccessibilityServiceConnection extends AbstractAccessibilityServiceConnection {
    private static final String LOG_TAG = "AccessibilityServiceConnection";
    final Intent mIntent = new Intent().setComponent(this.mComponentName);
    private final Handler mMainHandler;
    final WeakReference<AccessibilityManagerService.UserState> mUserStateWeakReference;
    private boolean mWasConnectedAndDied;

    public AccessibilityServiceConnection(AccessibilityManagerService.UserState userState, Context context, ComponentName componentName, AccessibilityServiceInfo accessibilityServiceInfo, int id, Handler mainHandler, Object lock, AccessibilityManagerService.SecurityPolicy securityPolicy, AbstractAccessibilityServiceConnection.SystemSupport systemSupport, WindowManagerInternal windowManagerInternal, GlobalActionPerformer globalActionPerfomer) {
        super(context, componentName, accessibilityServiceInfo, id, mainHandler, lock, securityPolicy, systemSupport, windowManagerInternal, globalActionPerfomer);
        this.mUserStateWeakReference = new WeakReference<>(userState);
        this.mMainHandler = mainHandler;
        this.mIntent.putExtra("android.intent.extra.client_label", 17039532);
        long identity = Binder.clearCallingIdentity();
        try {
            this.mIntent.putExtra("android.intent.extra.client_intent", this.mSystemSupport.getPendingIntentActivity(this.mContext, 0, new Intent("android.settings.ACCESSIBILITY_SETTINGS"), 0));
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void bindLocked() {
        AccessibilityManagerService.UserState userState = this.mUserStateWeakReference.get();
        if (userState != null) {
            long identity = Binder.clearCallingIdentity();
            int flags = 34603009;
            try {
                if (userState.getBindInstantServiceAllowed()) {
                    flags = 34603009 | DumpState.DUMP_CHANGES;
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
        AccessibilityManagerService.UserState userState = this.mUserStateWeakReference.get();
        if (userState != null) {
            userState.removeServiceLocked(this);
            this.mSystemSupport.getMagnificationController().resetAllIfNeeded(this.mId);
            resetLocked();
        }
    }

    public boolean canRetrieveInteractiveWindowsLocked() {
        return this.mSecurityPolicy.canRetrieveWindowContentLocked(this) && this.mRetrieveInteractiveWindows;
    }

    /* JADX INFO: finally extract failed */
    public void disableSelf() {
        synchronized (this.mLock) {
            AccessibilityManagerService.UserState userState = this.mUserStateWeakReference.get();
            if (userState != null) {
                if (userState.getEnabledServicesLocked().remove(this.mComponentName)) {
                    long identity = Binder.clearCallingIdentity();
                    try {
                        this.mSystemSupport.persistComponentNamesToSettingLocked("enabled_accessibility_services", userState.getEnabledServicesLocked(), userState.mUserId);
                        Binder.restoreCallingIdentity(identity);
                        this.mSystemSupport.onClientChangeLocked(false);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(identity);
                        throw th;
                    }
                }
            }
        }
    }

    @Override // android.content.ServiceConnection
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        synchronized (this.mLock) {
            if (this.mService != service) {
                if (this.mService != null) {
                    try {
                        this.mService.unlinkToDeath(this, 0);
                    } catch (NoSuchElementException e) {
                        Slog.e(LOG_TAG, "Failed unregister death link");
                    }
                }
                this.mService = service;
                try {
                    this.mService.linkToDeath(this, 0);
                } catch (RemoteException e2) {
                    Slog.e(LOG_TAG, "Failed registering death link");
                    binderDied();
                    return;
                }
            }
            this.mServiceInterface = IAccessibilityServiceClient.Stub.asInterface(service);
            AccessibilityManagerService.UserState userState = this.mUserStateWeakReference.get();
            if (userState != null) {
                userState.addServiceLocked(this);
                this.mSystemSupport.onClientChangeLocked(false);
                this.mMainHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$AccessibilityServiceConnection$ASP9bmSvpeD7ZE_uJ8sm9hCwiU.INSTANCE, this));
            }
        }
    }

    @Override // com.android.server.accessibility.AbstractAccessibilityServiceConnection
    public AccessibilityServiceInfo getServiceInfo() {
        this.mAccessibilityServiceInfo.crashed = this.mWasConnectedAndDied;
        return this.mAccessibilityServiceInfo;
    }

    /* access modifiers changed from: private */
    public void initializeService() {
        IAccessibilityServiceClient serviceInterface = null;
        synchronized (this.mLock) {
            AccessibilityManagerService.UserState userState = this.mUserStateWeakReference.get();
            if (userState != null) {
                Set<ComponentName> bindingServices = userState.getBindingServicesLocked();
                if (bindingServices.contains(this.mComponentName) || this.mWasConnectedAndDied) {
                    bindingServices.remove(this.mComponentName);
                    this.mWasConnectedAndDied = false;
                    serviceInterface = this.mServiceInterface;
                }
                if (serviceInterface != null && !userState.getEnabledServicesLocked().contains(this.mComponentName)) {
                    this.mSystemSupport.onClientChangeLocked(false);
                    return;
                }
            } else {
                return;
            }
        }
        if (serviceInterface == null) {
            binderDied();
            return;
        }
        try {
            serviceInterface.init(this, this.mId, this.mOverlayWindowToken);
        } catch (RemoteException re) {
            Slog.w(LOG_TAG, "Error while setting connection for service: " + serviceInterface, re);
            binderDied();
        }
    }

    @Override // android.content.ServiceConnection
    public void onServiceDisconnected(ComponentName componentName) {
        binderDied();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.accessibility.AbstractAccessibilityServiceConnection
    public boolean isCalledForCurrentUserLocked() {
        return this.mSecurityPolicy.resolveCallingUserIdEnforcingPermissionsLocked(-2) == this.mSystemSupport.getCurrentUserIdLocked();
    }

    public boolean setSoftKeyboardShowMode(int showMode) {
        synchronized (this.mLock) {
            if (!isCalledForCurrentUserLocked()) {
                return false;
            }
            AccessibilityManagerService.UserState userState = this.mUserStateWeakReference.get();
            if (userState == null) {
                return false;
            }
            return userState.setSoftKeyboardModeLocked(showMode, this.mComponentName);
        }
    }

    public int getSoftKeyboardShowMode() {
        AccessibilityManagerService.UserState userState = this.mUserStateWeakReference.get();
        if (userState != null) {
            return userState.getSoftKeyboardShowMode();
        }
        return 0;
    }

    public boolean isAccessibilityButtonAvailable() {
        synchronized (this.mLock) {
            boolean z = false;
            if (!isCalledForCurrentUserLocked()) {
                return false;
            }
            AccessibilityManagerService.UserState userState = this.mUserStateWeakReference.get();
            if (userState != null && isAccessibilityButtonAvailableLocked(userState)) {
                z = true;
            }
            return z;
        }
    }

    @Override // android.os.IBinder.DeathRecipient
    public void binderDied() {
        synchronized (this.mLock) {
            if (isConnectedLocked()) {
                this.mWasConnectedAndDied = true;
                AccessibilityManagerService.UserState userState = this.mUserStateWeakReference.get();
                if (userState != null) {
                    userState.serviceDisconnectedLocked(this);
                }
                resetLocked();
                this.mSystemSupport.getMagnificationController().resetAllIfNeeded(this.mId);
                if (userState != null && userState.getInputFilterFlag() == 0) {
                    Slog.i(LOG_TAG, "volume_debug binder Died, set Filter to null.");
                    this.mWindowManagerService.setInputFilter((IInputFilter) null);
                }
                this.mSystemSupport.onClientChangeLocked(false);
            }
        }
    }

    public boolean isAccessibilityButtonAvailableLocked(AccessibilityManagerService.UserState userState) {
        if (!(this.mRequestAccessibilityButton && this.mSystemSupport.isAccessibilityButtonShown())) {
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

    @Override // com.android.server.accessibility.FingerprintGestureDispatcher.FingerprintGestureClient
    public boolean isCapturingFingerprintGestures() {
        return this.mServiceInterface != null && this.mSecurityPolicy.canCaptureFingerprintGestures(this) && this.mCaptureFingerprintGestures;
    }

    @Override // com.android.server.accessibility.FingerprintGestureDispatcher.FingerprintGestureClient
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

    @Override // com.android.server.accessibility.FingerprintGestureDispatcher.FingerprintGestureClient
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

    @Override // com.android.server.accessibility.AbstractAccessibilityServiceConnection
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
