package android.view.accessibility;

import android.Manifest;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import android.view.IWindow;
import android.view.accessibility.IAccessibilityManager;
import android.view.accessibility.IAccessibilityManagerClient;
import com.android.internal.R;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.IntPair;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AccessibilityManager {
    public static final String ACTION_CHOOSE_ACCESSIBILITY_BUTTON = "com.android.internal.intent.action.CHOOSE_ACCESSIBILITY_BUTTON";
    public static final int AUTOCLICK_DELAY_DEFAULT = 600;
    public static final int DALTONIZER_CORRECT_DEUTERANOMALY = 12;
    public static final int DALTONIZER_DISABLED = -1;
    @UnsupportedAppUsage
    public static final int DALTONIZER_SIMULATE_MONOCHROMACY = 0;
    private static final boolean DEBUG = false;
    public static final int FLAG_CONTENT_CONTROLS = 4;
    public static final int FLAG_CONTENT_ICONS = 1;
    public static final int FLAG_CONTENT_TEXT = 2;
    private static final String LOG_TAG = "AccessibilityManager";
    public static final int STATE_FLAG_ACCESSIBILITY_ENABLED = 1;
    public static final int STATE_FLAG_HIGH_TEXT_CONTRAST_ENABLED = 4;
    public static final int STATE_FLAG_TOUCH_EXPLORATION_ENABLED = 2;
    @UnsupportedAppUsage
    private static AccessibilityManager sInstance;
    @UnsupportedAppUsage
    static final Object sInstanceSync = new Object();
    AccessibilityPolicy mAccessibilityPolicy;
    @UnsupportedAppUsage
    private final ArrayMap<AccessibilityStateChangeListener, Handler> mAccessibilityStateChangeListeners = new ArrayMap<>();
    final Handler.Callback mCallback = new MyCallback();
    private final IAccessibilityManagerClient.Stub mClient = new IAccessibilityManagerClient.Stub() {
        /* class android.view.accessibility.AccessibilityManager.AnonymousClass1 */

        @Override // android.view.accessibility.IAccessibilityManagerClient
        public void setState(int state) {
            AccessibilityManager.this.mHandler.obtainMessage(1, state, 0).sendToTarget();
        }

        /* JADX WARNING: Code restructure failed: missing block: B:10:0x002b, code lost:
            if (r2 >= r0) goto L_0x0050;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x002d, code lost:
            ((android.os.Handler) r6.this$0.mServicesStateChangeListeners.valueAt(r2)).post(new android.view.accessibility.$$Lambda$AccessibilityManager$1$o7fCplskH9NlBwJvkl6NoZ0L_BA(r6, (android.view.accessibility.AccessibilityManager.AccessibilityServicesStateChangeListener) r6.this$0.mServicesStateChangeListeners.keyAt(r2)));
            r2 = r2 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0050, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x0026, code lost:
            r0 = r1.size();
            r2 = 0;
         */
        @Override // android.view.accessibility.IAccessibilityManagerClient
        public void notifyServicesStateChanged(long updatedUiTimeout) {
            AccessibilityManager.this.updateUiTimeout(updatedUiTimeout);
            synchronized (AccessibilityManager.this.mLock) {
                if (!AccessibilityManager.this.mServicesStateChangeListeners.isEmpty()) {
                    ArrayMap<AccessibilityServicesStateChangeListener, Handler> listeners = new ArrayMap<>(AccessibilityManager.this.mServicesStateChangeListeners);
                }
            }
        }

        public /* synthetic */ void lambda$notifyServicesStateChanged$0$AccessibilityManager$1(AccessibilityServicesStateChangeListener listener) {
            listener.onAccessibilityServicesStateChanged(AccessibilityManager.this);
        }

        @Override // android.view.accessibility.IAccessibilityManagerClient
        public void setRelevantEventTypes(int eventTypes) {
            AccessibilityManager.this.mRelevantEventTypes = eventTypes;
        }
    };
    @UnsupportedAppUsage
    final Handler mHandler;
    private final ArrayMap<HighTextContrastChangeListener, Handler> mHighTextContrastStateChangeListeners = new ArrayMap<>();
    int mInteractiveUiTimeout;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    boolean mIsEnabled;
    @UnsupportedAppUsage(trackingBug = 123768939)
    boolean mIsHighTextContrastEnabled;
    boolean mIsTouchExplorationEnabled;
    @UnsupportedAppUsage
    private final Object mLock = new Object();
    int mNonInteractiveUiTimeout;
    int mRelevantEventTypes = -1;
    private SparseArray<List<AccessibilityRequestPreparer>> mRequestPreparerLists;
    @UnsupportedAppUsage
    private IAccessibilityManager mService;
    private final ArrayMap<AccessibilityServicesStateChangeListener, Handler> mServicesStateChangeListeners = new ArrayMap<>();
    private final ArrayMap<TouchExplorationStateChangeListener, Handler> mTouchExplorationStateChangeListeners = new ArrayMap<>();
    @UnsupportedAppUsage
    final int mUserId;

    public interface AccessibilityPolicy {
        List<AccessibilityServiceInfo> getEnabledAccessibilityServiceList(int i, List<AccessibilityServiceInfo> list);

        List<AccessibilityServiceInfo> getInstalledAccessibilityServiceList(List<AccessibilityServiceInfo> list);

        int getRelevantEventTypes(int i);

        boolean isEnabled(boolean z);

        AccessibilityEvent onAccessibilityEvent(AccessibilityEvent accessibilityEvent, boolean z, int i);
    }

    public interface AccessibilityServicesStateChangeListener {
        void onAccessibilityServicesStateChanged(AccessibilityManager accessibilityManager);
    }

    public interface AccessibilityStateChangeListener {
        void onAccessibilityStateChanged(boolean z);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ContentFlag {
    }

    public interface HighTextContrastChangeListener {
        void onHighTextContrastStateChanged(boolean z);
    }

    public interface TouchExplorationStateChangeListener {
        void onTouchExplorationStateChanged(boolean z);
    }

    @UnsupportedAppUsage
    public static AccessibilityManager getInstance(Context context) {
        int userId;
        synchronized (sInstanceSync) {
            if (sInstance == null) {
                if (!(Binder.getCallingUid() == 1000 || context.checkCallingOrSelfPermission(Manifest.permission.INTERACT_ACROSS_USERS) == 0)) {
                    if (context.checkCallingOrSelfPermission(Manifest.permission.INTERACT_ACROSS_USERS_FULL) != 0) {
                        userId = context.getUserId();
                        sInstance = new AccessibilityManager(context, (IAccessibilityManager) null, userId);
                    }
                }
                userId = -2;
                sInstance = new AccessibilityManager(context, (IAccessibilityManager) null, userId);
            }
        }
        return sInstance;
    }

    public AccessibilityManager(Context context, IAccessibilityManager service, int userId) {
        this.mHandler = new Handler(context.getMainLooper(), this.mCallback);
        this.mUserId = userId;
        synchronized (this.mLock) {
            tryConnectToServiceLocked(service);
        }
    }

    public AccessibilityManager(Handler handler, IAccessibilityManager service, int userId) {
        this.mHandler = handler;
        this.mUserId = userId;
        synchronized (this.mLock) {
            tryConnectToServiceLocked(service);
        }
    }

    public IAccessibilityManagerClient getClient() {
        return this.mClient;
    }

    @VisibleForTesting
    public Handler.Callback getCallback() {
        return this.mCallback;
    }

    public boolean isEnabled() {
        boolean z;
        synchronized (this.mLock) {
            if (!this.mIsEnabled) {
                if (this.mAccessibilityPolicy == null || !this.mAccessibilityPolicy.isEnabled(this.mIsEnabled)) {
                    z = false;
                }
            }
            z = true;
        }
        return z;
    }

    public boolean isTouchExplorationEnabled() {
        synchronized (this.mLock) {
            if (getServiceLocked() == null) {
                return false;
            }
            return this.mIsTouchExplorationEnabled;
        }
    }

    @UnsupportedAppUsage
    public boolean isHighTextContrastEnabled() {
        synchronized (this.mLock) {
            if (getServiceLocked() == null) {
                return false;
            }
            return this.mIsHighTextContrastEnabled;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:30:?, code lost:
        r4 = android.os.Binder.clearCallingIdentity();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:?, code lost:
        r1.sendAccessibilityEvent(r2, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x005f, code lost:
        if (r8 == r2) goto L_0x0064;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0061, code lost:
        r8.recycle();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0064, code lost:
        r2.recycle();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0068, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0069, code lost:
        android.os.Binder.restoreCallingIdentity(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x006c, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x006d, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x006f, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:?, code lost:
        android.util.Log.e(android.view.accessibility.AccessibilityManager.LOG_TAG, "Error during sending " + r2 + android.net.wifi.WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x008c, code lost:
        if (r8 == r2) goto L_0x0064;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x008f, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x0090, code lost:
        if (r8 != r2) goto L_0x0092;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0092, code lost:
        r8.recycle();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0095, code lost:
        r2.recycle();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0098, code lost:
        throw r0;
     */
    public void sendAccessibilityEvent(AccessibilityEvent event) {
        AccessibilityEvent dispatchedEvent;
        synchronized (this.mLock) {
            IAccessibilityManager service = getServiceLocked();
            if (service != null) {
                event.setEventTime(SystemClock.uptimeMillis());
                if (this.mAccessibilityPolicy != null) {
                    dispatchedEvent = this.mAccessibilityPolicy.onAccessibilityEvent(event, this.mIsEnabled, this.mRelevantEventTypes);
                    if (dispatchedEvent == null) {
                        return;
                    }
                } else {
                    dispatchedEvent = event;
                }
                if (!isEnabled()) {
                    if (Looper.myLooper() != Looper.getMainLooper()) {
                        Log.e(LOG_TAG, "AccessibilityEvent sent with accessibility disabled");
                        return;
                    }
                    throw new IllegalStateException("Accessibility off. Did you forget to check that?");
                } else if ((dispatchedEvent.getEventType() & this.mRelevantEventTypes) != 0) {
                    int userId = this.mUserId;
                }
            }
        }
    }

    /* JADX INFO: Multiple debug info for r2v1 int: [D('myLooper' android.os.Looper), D('userId' int)] */
    public void interrupt() {
        synchronized (this.mLock) {
            IAccessibilityManager service = getServiceLocked();
            if (service != null) {
                if (isEnabled()) {
                    int userId = this.mUserId;
                    try {
                        service.interrupt(userId);
                    } catch (RemoteException re) {
                        Log.e(LOG_TAG, "Error while requesting interrupt from all services. ", re);
                    }
                } else if (Looper.myLooper() != Looper.getMainLooper()) {
                    Log.e(LOG_TAG, "Interrupt called with accessibility disabled");
                } else {
                    throw new IllegalStateException("Accessibility off. Did you forget to check that?");
                }
            }
        }
    }

    @Deprecated
    public List<ServiceInfo> getAccessibilityServiceList() {
        List<AccessibilityServiceInfo> infos = getInstalledAccessibilityServiceList();
        List<ServiceInfo> services = new ArrayList<>();
        int infoCount = infos.size();
        for (int i = 0; i < infoCount; i++) {
            services.add(infos.get(i).getResolveInfo().serviceInfo);
        }
        return Collections.unmodifiableList(services);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0012, code lost:
        r0 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0017, code lost:
        r0 = r1.getInstalledAccessibilityServiceList(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0019, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x001a, code lost:
        android.util.Log.e(android.view.accessibility.AccessibilityManager.LOG_TAG, "Error while obtaining the installed AccessibilityServices. ", r3);
     */
    public List<AccessibilityServiceInfo> getInstalledAccessibilityServiceList() {
        List<AccessibilityServiceInfo> services;
        synchronized (this.mLock) {
            IAccessibilityManager service = getServiceLocked();
            if (service == null) {
                return Collections.emptyList();
            }
            int userId = this.mUserId;
        }
        AccessibilityPolicy accessibilityPolicy = this.mAccessibilityPolicy;
        if (accessibilityPolicy != null) {
            services = accessibilityPolicy.getInstalledAccessibilityServiceList(services);
        }
        if (services != null) {
            return Collections.unmodifiableList(services);
        }
        return Collections.emptyList();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0012, code lost:
        r0 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0017, code lost:
        r0 = r1.getEnabledAccessibilityServiceList(r7, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0019, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x001a, code lost:
        android.util.Log.e(android.view.accessibility.AccessibilityManager.LOG_TAG, "Error while obtaining the installed AccessibilityServices. ", r3);
     */
    public List<AccessibilityServiceInfo> getEnabledAccessibilityServiceList(int feedbackTypeFlags) {
        List<AccessibilityServiceInfo> services;
        synchronized (this.mLock) {
            IAccessibilityManager service = getServiceLocked();
            if (service == null) {
                return Collections.emptyList();
            }
            int userId = this.mUserId;
        }
        AccessibilityPolicy accessibilityPolicy = this.mAccessibilityPolicy;
        if (accessibilityPolicy != null) {
            services = accessibilityPolicy.getEnabledAccessibilityServiceList(feedbackTypeFlags, services);
        }
        if (services != null) {
            return Collections.unmodifiableList(services);
        }
        return Collections.emptyList();
    }

    public boolean addAccessibilityStateChangeListener(AccessibilityStateChangeListener listener) {
        addAccessibilityStateChangeListener(listener, null);
        return true;
    }

    public void addAccessibilityStateChangeListener(AccessibilityStateChangeListener listener, Handler handler) {
        synchronized (this.mLock) {
            this.mAccessibilityStateChangeListeners.put(listener, handler == null ? this.mHandler : handler);
        }
    }

    public boolean removeAccessibilityStateChangeListener(AccessibilityStateChangeListener listener) {
        boolean z;
        synchronized (this.mLock) {
            int index = this.mAccessibilityStateChangeListeners.indexOfKey(listener);
            this.mAccessibilityStateChangeListeners.remove(listener);
            z = index >= 0;
        }
        return z;
    }

    public boolean addTouchExplorationStateChangeListener(TouchExplorationStateChangeListener listener) {
        addTouchExplorationStateChangeListener(listener, null);
        return true;
    }

    public void addTouchExplorationStateChangeListener(TouchExplorationStateChangeListener listener, Handler handler) {
        synchronized (this.mLock) {
            this.mTouchExplorationStateChangeListeners.put(listener, handler == null ? this.mHandler : handler);
        }
    }

    public boolean removeTouchExplorationStateChangeListener(TouchExplorationStateChangeListener listener) {
        boolean z;
        synchronized (this.mLock) {
            int index = this.mTouchExplorationStateChangeListeners.indexOfKey(listener);
            this.mTouchExplorationStateChangeListeners.remove(listener);
            z = index >= 0;
        }
        return z;
    }

    public void addAccessibilityServicesStateChangeListener(AccessibilityServicesStateChangeListener listener, Handler handler) {
        synchronized (this.mLock) {
            this.mServicesStateChangeListeners.put(listener, handler == null ? this.mHandler : handler);
        }
    }

    public void removeAccessibilityServicesStateChangeListener(AccessibilityServicesStateChangeListener listener) {
        synchronized (this.mLock) {
            this.mServicesStateChangeListeners.remove(listener);
        }
    }

    public void addAccessibilityRequestPreparer(AccessibilityRequestPreparer preparer) {
        if (this.mRequestPreparerLists == null) {
            this.mRequestPreparerLists = new SparseArray<>(1);
        }
        int id = preparer.getAccessibilityViewId();
        List<AccessibilityRequestPreparer> requestPreparerList = this.mRequestPreparerLists.get(id);
        if (requestPreparerList == null) {
            requestPreparerList = new ArrayList(1);
            this.mRequestPreparerLists.put(id, requestPreparerList);
        }
        requestPreparerList.add(preparer);
    }

    public void removeAccessibilityRequestPreparer(AccessibilityRequestPreparer preparer) {
        int viewId;
        List<AccessibilityRequestPreparer> requestPreparerList;
        if (this.mRequestPreparerLists != null && (requestPreparerList = this.mRequestPreparerLists.get((viewId = preparer.getAccessibilityViewId()))) != null) {
            requestPreparerList.remove(preparer);
            if (requestPreparerList.isEmpty()) {
                this.mRequestPreparerLists.remove(viewId);
            }
        }
    }

    public int getRecommendedTimeoutMillis(int originalTimeout, int uiContentFlags) {
        boolean hasIconsOrText = false;
        boolean hasControls = (uiContentFlags & 4) != 0;
        if (!((uiContentFlags & 1) == 0 && (uiContentFlags & 2) == 0)) {
            hasIconsOrText = true;
        }
        int recommendedTimeout = originalTimeout;
        if (hasControls) {
            recommendedTimeout = Math.max(recommendedTimeout, this.mInteractiveUiTimeout);
        }
        if (hasIconsOrText) {
            return Math.max(recommendedTimeout, this.mNonInteractiveUiTimeout);
        }
        return recommendedTimeout;
    }

    public List<AccessibilityRequestPreparer> getRequestPreparersForAccessibilityId(int id) {
        SparseArray<List<AccessibilityRequestPreparer>> sparseArray = this.mRequestPreparerLists;
        if (sparseArray == null) {
            return null;
        }
        return sparseArray.get(id);
    }

    public void addHighTextContrastStateChangeListener(HighTextContrastChangeListener listener, Handler handler) {
        synchronized (this.mLock) {
            this.mHighTextContrastStateChangeListeners.put(listener, handler == null ? this.mHandler : handler);
        }
    }

    public void removeHighTextContrastStateChangeListener(HighTextContrastChangeListener listener) {
        synchronized (this.mLock) {
            this.mHighTextContrastStateChangeListeners.remove(listener);
        }
    }

    public void setAccessibilityPolicy(AccessibilityPolicy policy) {
        synchronized (this.mLock) {
            this.mAccessibilityPolicy = policy;
        }
    }

    public boolean isAccessibilityVolumeStreamActive() {
        List<AccessibilityServiceInfo> serviceInfos = getEnabledAccessibilityServiceList(-1);
        for (int i = 0; i < serviceInfos.size(); i++) {
            if ((serviceInfos.get(i).flags & 128) != 0) {
                return true;
            }
        }
        return false;
    }

    public boolean sendFingerprintGesture(int keyCode) {
        synchronized (this.mLock) {
            IAccessibilityManager service = getServiceLocked();
            if (service == null) {
                return false;
            }
            try {
                return service.sendFingerprintGesture(keyCode);
            } catch (RemoteException e) {
                return false;
            }
        }
    }

    @SystemApi
    public int getAccessibilityWindowId(IBinder windowToken) {
        if (windowToken == null) {
            return -1;
        }
        synchronized (this.mLock) {
            IAccessibilityManager service = getServiceLocked();
            if (service == null) {
                return -1;
            }
            try {
                return service.getAccessibilityWindowId(windowToken);
            } catch (RemoteException e) {
                return -1;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private void setStateLocked(int stateFlags) {
        boolean highTextContrastEnabled = false;
        boolean enabled = (stateFlags & 1) != 0;
        boolean touchExplorationEnabled = (stateFlags & 2) != 0;
        if ((stateFlags & 4) != 0) {
            highTextContrastEnabled = true;
        }
        boolean wasEnabled = isEnabled();
        boolean wasTouchExplorationEnabled = this.mIsTouchExplorationEnabled;
        boolean wasHighTextContrastEnabled = this.mIsHighTextContrastEnabled;
        this.mIsEnabled = enabled;
        this.mIsTouchExplorationEnabled = touchExplorationEnabled;
        this.mIsHighTextContrastEnabled = highTextContrastEnabled;
        if (wasEnabled != isEnabled()) {
            notifyAccessibilityStateChanged();
        }
        if (wasTouchExplorationEnabled != touchExplorationEnabled) {
            notifyTouchExplorationStateChanged();
        }
        if (wasHighTextContrastEnabled != highTextContrastEnabled) {
            notifyHighTextContrastStateChanged();
        }
    }

    public AccessibilityServiceInfo getInstalledServiceInfoWithComponentName(ComponentName componentName) {
        List<AccessibilityServiceInfo> installedServiceInfos = getInstalledAccessibilityServiceList();
        if (installedServiceInfos == null || componentName == null) {
            return null;
        }
        for (int i = 0; i < installedServiceInfos.size(); i++) {
            if (componentName.equals(installedServiceInfos.get(i).getComponentName())) {
                return installedServiceInfos.get(i);
            }
        }
        return null;
    }

    public int addAccessibilityInteractionConnection(IWindow windowToken, String packageName, IAccessibilityInteractionConnection connection) {
        synchronized (this.mLock) {
            IAccessibilityManager service = getServiceLocked();
            if (service == null) {
                return -1;
            }
            int userId = this.mUserId;
            try {
                return service.addAccessibilityInteractionConnection(windowToken, connection, packageName, userId);
            } catch (RemoteException re) {
                Log.e(LOG_TAG, "Error while adding an accessibility interaction connection. ", re);
                return -1;
            }
        }
    }

    public void removeAccessibilityInteractionConnection(IWindow windowToken) {
        synchronized (this.mLock) {
            IAccessibilityManager service = getServiceLocked();
            if (service != null) {
                try {
                    service.removeAccessibilityInteractionConnection(windowToken);
                } catch (RemoteException re) {
                    Log.e(LOG_TAG, "Error while removing an accessibility interaction connection. ", re);
                }
            }
        }
    }

    @SystemApi
    public void performAccessibilityShortcut() {
        synchronized (this.mLock) {
            IAccessibilityManager service = getServiceLocked();
            if (service != null) {
                try {
                    service.performAccessibilityShortcut();
                } catch (RemoteException re) {
                    Log.e(LOG_TAG, "Error performing accessibility shortcut. ", re);
                }
            }
        }
    }

    public void notifyAccessibilityButtonClicked(int displayId) {
        synchronized (this.mLock) {
            IAccessibilityManager service = getServiceLocked();
            if (service != null) {
                try {
                    service.notifyAccessibilityButtonClicked(displayId);
                } catch (RemoteException re) {
                    Log.e(LOG_TAG, "Error while dispatching accessibility button click", re);
                }
            }
        }
    }

    public void notifyAccessibilityButtonVisibilityChanged(boolean shown) {
        synchronized (this.mLock) {
            IAccessibilityManager service = getServiceLocked();
            if (service != null) {
                try {
                    service.notifyAccessibilityButtonVisibilityChanged(shown);
                } catch (RemoteException re) {
                    Log.e(LOG_TAG, "Error while dispatching accessibility button visibility change", re);
                }
            }
        }
    }

    public void setPictureInPictureActionReplacingConnection(IAccessibilityInteractionConnection connection) {
        synchronized (this.mLock) {
            IAccessibilityManager service = getServiceLocked();
            if (service != null) {
                try {
                    service.setPictureInPictureActionReplacingConnection(connection);
                } catch (RemoteException re) {
                    Log.e(LOG_TAG, "Error setting picture in picture action replacement", re);
                }
            }
        }
    }

    public String getAccessibilityShortcutService() {
        IAccessibilityManager service;
        synchronized (this.mLock) {
            service = getServiceLocked();
        }
        if (service == null) {
            return null;
        }
        try {
            return service.getAccessibilityShortcutService();
        } catch (RemoteException re) {
            re.rethrowFromSystemServer();
            return null;
        }
    }

    private IAccessibilityManager getServiceLocked() {
        if (this.mService == null) {
            tryConnectToServiceLocked(null);
        }
        return this.mService;
    }

    private void tryConnectToServiceLocked(IAccessibilityManager service) {
        if (service == null) {
            IBinder iBinder = ServiceManager.getService(Context.ACCESSIBILITY_SERVICE);
            if (iBinder != null) {
                service = IAccessibilityManager.Stub.asInterface(iBinder);
            } else {
                return;
            }
        }
        try {
            long userStateAndRelevantEvents = service.addClient(this.mClient, this.mUserId);
            setStateLocked(IntPair.first(userStateAndRelevantEvents));
            this.mRelevantEventTypes = IntPair.second(userStateAndRelevantEvents);
            updateUiTimeout(service.getRecommendedTimeoutMillis());
            this.mService = service;
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "AccessibilityManagerService is dead", re);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001e, code lost:
        if (r3 >= r0) goto L_0x0037;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0020, code lost:
        r2.valueAt(r3).post(new android.view.accessibility.$$Lambda$AccessibilityManager$yzw5NYY7_MfAQ9gLy3mVllchaXo(r2.keyAt(r3), r1));
        r3 = r3 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0037, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0019, code lost:
        r0 = r2.size();
        r3 = 0;
     */
    private void notifyAccessibilityStateChanged() {
        synchronized (this.mLock) {
            if (!this.mAccessibilityStateChangeListeners.isEmpty()) {
                boolean isEnabled = isEnabled();
                ArrayMap<AccessibilityStateChangeListener, Handler> listeners = new ArrayMap<>(this.mAccessibilityStateChangeListeners);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001c, code lost:
        if (r3 >= r0) goto L_0x0035;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001e, code lost:
        r2.valueAt(r3).post(new android.view.accessibility.$$Lambda$AccessibilityManager$a0OtrjOl35tiW2vwyvAmY6_LiLI(r2.keyAt(r3), r1));
        r3 = r3 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0035, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0017, code lost:
        r0 = r2.size();
        r3 = 0;
     */
    private void notifyTouchExplorationStateChanged() {
        synchronized (this.mLock) {
            if (!this.mTouchExplorationStateChangeListeners.isEmpty()) {
                boolean isTouchExplorationEnabled = this.mIsTouchExplorationEnabled;
                ArrayMap<TouchExplorationStateChangeListener, Handler> listeners = new ArrayMap<>(this.mTouchExplorationStateChangeListeners);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001c, code lost:
        if (r3 >= r0) goto L_0x0035;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001e, code lost:
        r2.valueAt(r3).post(new android.view.accessibility.$$Lambda$AccessibilityManager$4M6GrmFiqsRwVzn352N10DcU6RM(r2.keyAt(r3), r1));
        r3 = r3 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0035, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0017, code lost:
        r0 = r2.size();
        r3 = 0;
     */
    private void notifyHighTextContrastStateChanged() {
        synchronized (this.mLock) {
            if (!this.mHighTextContrastStateChangeListeners.isEmpty()) {
                boolean isHighTextContrastEnabled = this.mIsHighTextContrastEnabled;
                ArrayMap<HighTextContrastChangeListener, Handler> listeners = new ArrayMap<>(this.mHighTextContrastStateChangeListeners);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateUiTimeout(long uiTimeout) {
        this.mInteractiveUiTimeout = IntPair.first(uiTimeout);
        this.mNonInteractiveUiTimeout = IntPair.second(uiTimeout);
    }

    public static boolean isAccessibilityButtonSupported() {
        return Resources.getSystem().getBoolean(R.bool.config_showNavigationBar);
    }

    private final class MyCallback implements Handler.Callback {
        public static final int MSG_SET_STATE = 1;

        private MyCallback() {
        }

        @Override // android.os.Handler.Callback
        public boolean handleMessage(Message message) {
            if (message.what == 1) {
                int state = message.arg1;
                synchronized (AccessibilityManager.this.mLock) {
                    AccessibilityManager.this.setStateLocked(state);
                }
            }
            return true;
        }
    }
}
