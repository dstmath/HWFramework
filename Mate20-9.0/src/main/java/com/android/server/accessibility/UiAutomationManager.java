package com.android.server.accessibility;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.IAccessibilityServiceClient;
import android.content.ComponentName;
import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Slog;
import android.view.accessibility.AccessibilityEvent;
import com.android.internal.util.DumpUtils;
import com.android.server.accessibility.AbstractAccessibilityServiceConnection;
import com.android.server.accessibility.AccessibilityManagerService;
import com.android.server.accessibility.UiAutomationManager;
import com.android.server.wm.WindowManagerInternal;
import java.io.FileDescriptor;
import java.io.PrintWriter;

class UiAutomationManager {
    /* access modifiers changed from: private */
    public static final ComponentName COMPONENT_NAME = new ComponentName("com.android.server.accessibility", "UiAutomation");
    private static final String LOG_TAG = "UiAutomationManager";
    private AbstractAccessibilityServiceConnection.SystemSupport mSystemSupport;
    private int mUiAutomationFlags;
    /* access modifiers changed from: private */
    public UiAutomationService mUiAutomationService;
    private AccessibilityServiceInfo mUiAutomationServiceInfo;
    /* access modifiers changed from: private */
    public IBinder mUiAutomationServiceOwner;
    private final IBinder.DeathRecipient mUiAutomationServiceOwnerDeathRecipient = new IBinder.DeathRecipient() {
        public void binderDied() {
            try {
                UiAutomationManager.this.mUiAutomationServiceOwner.unlinkToDeath(this, 0);
            } catch (Exception e) {
                Slog.e(UiAutomationManager.LOG_TAG, "unlinkToDeath server error", e);
            }
            IBinder unused = UiAutomationManager.this.mUiAutomationServiceOwner = null;
            if (UiAutomationManager.this.mUiAutomationService != null) {
                UiAutomationManager.this.destroyUiAutomationService();
            }
        }
    };

    private class UiAutomationService extends AbstractAccessibilityServiceConnection {
        private final Handler mMainHandler;
        final /* synthetic */ UiAutomationManager this$0;

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        UiAutomationService(UiAutomationManager uiAutomationManager, Context context, AccessibilityServiceInfo accessibilityServiceInfo, int id, Handler mainHandler, Object lock, AccessibilityManagerService.SecurityPolicy securityPolicy, AbstractAccessibilityServiceConnection.SystemSupport systemSupport, WindowManagerInternal windowManagerInternal, GlobalActionPerformer globalActionPerfomer) {
            super(context, UiAutomationManager.COMPONENT_NAME, accessibilityServiceInfo, id, mainHandler, lock, securityPolicy, systemSupport, windowManagerInternal, globalActionPerfomer);
            this.this$0 = uiAutomationManager;
            this.mMainHandler = mainHandler;
        }

        /* access modifiers changed from: package-private */
        public void connectServiceUnknownThread() {
            this.mMainHandler.post(new Runnable() {
                public final void run() {
                    UiAutomationManager.UiAutomationService.lambda$connectServiceUnknownThread$0(UiAutomationManager.UiAutomationService.this);
                }
            });
        }

        public static /* synthetic */ void lambda$connectServiceUnknownThread$0(UiAutomationService uiAutomationService) {
            IAccessibilityServiceClient serviceInterface;
            IBinder service;
            try {
                synchronized (uiAutomationService.mLock) {
                    serviceInterface = uiAutomationService.mServiceInterface;
                    uiAutomationService.mService = serviceInterface == null ? null : uiAutomationService.mServiceInterface.asBinder();
                    service = uiAutomationService.mService;
                }
                if (serviceInterface != null) {
                    service.linkToDeath(uiAutomationService, 0);
                    serviceInterface.init(uiAutomationService, uiAutomationService.mId, uiAutomationService.mOverlayWindowToken);
                }
            } catch (RemoteException re) {
                Slog.w(UiAutomationManager.LOG_TAG, "Error initialized connection", re);
                uiAutomationService.this$0.destroyUiAutomationService();
            }
        }

        public void binderDied() {
            this.this$0.destroyUiAutomationService();
        }

        /* access modifiers changed from: protected */
        public boolean isCalledForCurrentUserLocked() {
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean supportsFlagForNotImportantViews(AccessibilityServiceInfo info) {
            return true;
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpPermission(this.mContext, UiAutomationManager.LOG_TAG, pw)) {
                synchronized (this.mLock) {
                    pw.append("Ui Automation[eventTypes=" + AccessibilityEvent.eventTypeToString(this.mEventTypes));
                    pw.append(", notificationTimeout=" + this.mNotificationTimeout);
                    pw.append("]");
                }
            }
        }

        public boolean setSoftKeyboardShowMode(int mode) {
            return false;
        }

        public boolean isAccessibilityButtonAvailable() {
            return false;
        }

        public void disableSelf() {
        }

        public void onServiceConnected(ComponentName componentName, IBinder service) {
        }

        public void onServiceDisconnected(ComponentName componentName) {
        }

        public boolean isCapturingFingerprintGestures() {
            return false;
        }

        public void onFingerprintGestureDetectionActiveChanged(boolean active) {
        }

        public void onFingerprintGesture(int gesture) {
        }
    }

    UiAutomationManager() {
    }

    /* access modifiers changed from: package-private */
    public void registerUiTestAutomationServiceLocked(IBinder owner, IAccessibilityServiceClient serviceClient, Context context, AccessibilityServiceInfo accessibilityServiceInfo, int id, Handler mainHandler, Object lock, AccessibilityManagerService.SecurityPolicy securityPolicy, AbstractAccessibilityServiceConnection.SystemSupport systemSupport, WindowManagerInternal windowManagerInternal, GlobalActionPerformer globalActionPerfomer, int flags) {
        IBinder iBinder = owner;
        IAccessibilityServiceClient iAccessibilityServiceClient = serviceClient;
        AccessibilityServiceInfo accessibilityServiceInfo2 = accessibilityServiceInfo;
        accessibilityServiceInfo2.setComponentName(COMPONENT_NAME);
        if (this.mUiAutomationService == null) {
            try {
                iBinder.linkToDeath(this.mUiAutomationServiceOwnerDeathRecipient, 0);
                AbstractAccessibilityServiceConnection.SystemSupport systemSupport2 = systemSupport;
                this.mSystemSupport = systemSupport2;
                UiAutomationService uiAutomationService = new UiAutomationService(this, context, accessibilityServiceInfo2, id, mainHandler, lock, securityPolicy, systemSupport2, windowManagerInternal, globalActionPerfomer);
                this.mUiAutomationService = uiAutomationService;
                this.mUiAutomationServiceOwner = iBinder;
                this.mUiAutomationFlags = flags;
                this.mUiAutomationServiceInfo = accessibilityServiceInfo2;
                this.mUiAutomationService.mServiceInterface = iAccessibilityServiceClient;
                this.mUiAutomationService.onAdded();
                try {
                    this.mUiAutomationService.mServiceInterface.asBinder().linkToDeath(this.mUiAutomationService, 0);
                    this.mUiAutomationService.connectServiceUnknownThread();
                } catch (RemoteException re) {
                    Slog.e(LOG_TAG, "Failed registering death link: " + re);
                    destroyUiAutomationService();
                }
            } catch (RemoteException re2) {
                int i = flags;
                Slog.e(LOG_TAG, "Couldn't register for the death of a UiTestAutomationService!", re2);
            }
        } else {
            int i2 = flags;
            throw new IllegalStateException("UiAutomationService " + iAccessibilityServiceClient + "already registered!");
        }
    }

    /* access modifiers changed from: package-private */
    public void unregisterUiTestAutomationServiceLocked(IAccessibilityServiceClient serviceClient) {
        if (this.mUiAutomationService == null || serviceClient == null || this.mUiAutomationService.mServiceInterface == null || serviceClient.asBinder() != this.mUiAutomationService.mServiceInterface.asBinder()) {
            throw new IllegalStateException("UiAutomationService " + serviceClient + " not registered!");
        }
        destroyUiAutomationService();
    }

    /* access modifiers changed from: package-private */
    public void sendAccessibilityEventLocked(AccessibilityEvent event) {
        if (this.mUiAutomationService != null) {
            this.mUiAutomationService.notifyAccessibilityEvent(event);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isUiAutomationRunningLocked() {
        return this.mUiAutomationService != null;
    }

    /* access modifiers changed from: package-private */
    public boolean suppressingAccessibilityServicesLocked() {
        return this.mUiAutomationService != null && (this.mUiAutomationFlags & 1) == 0;
    }

    /* access modifiers changed from: package-private */
    public boolean isTouchExplorationEnabledLocked() {
        return this.mUiAutomationService != null && this.mUiAutomationService.mRequestTouchExplorationMode;
    }

    /* access modifiers changed from: package-private */
    public boolean canRetrieveInteractiveWindowsLocked() {
        return this.mUiAutomationService != null && this.mUiAutomationService.mRetrieveInteractiveWindows;
    }

    /* access modifiers changed from: package-private */
    public int getRequestedEventMaskLocked() {
        if (this.mUiAutomationService == null) {
            return 0;
        }
        return this.mUiAutomationService.mEventTypes;
    }

    /* access modifiers changed from: package-private */
    public int getRelevantEventTypes() {
        if (this.mUiAutomationService == null) {
            return 0;
        }
        return this.mUiAutomationService.getRelevantEventTypes();
    }

    /* access modifiers changed from: package-private */
    public AccessibilityServiceInfo getServiceInfo() {
        if (this.mUiAutomationService == null) {
            return null;
        }
        return this.mUiAutomationService.getServiceInfo();
    }

    /* access modifiers changed from: package-private */
    public void dumpUiAutomationService(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mUiAutomationService != null) {
            this.mUiAutomationService.dump(fd, pw, args);
        }
    }

    /* access modifiers changed from: private */
    public void destroyUiAutomationService() {
        try {
            this.mUiAutomationService.mServiceInterface.asBinder().unlinkToDeath(this.mUiAutomationService, 0);
            this.mUiAutomationService.onRemoved();
            this.mUiAutomationService.resetLocked();
            this.mUiAutomationService = null;
            this.mUiAutomationFlags = 0;
            if (this.mUiAutomationServiceOwner != null) {
                this.mUiAutomationServiceOwner.unlinkToDeath(this.mUiAutomationServiceOwnerDeathRecipient, 0);
                this.mUiAutomationServiceOwner = null;
            }
        } catch (Exception e) {
            Slog.e(LOG_TAG, "destroyUiAutomationService error", e);
        }
        this.mSystemSupport.onClientChange(false);
    }
}
