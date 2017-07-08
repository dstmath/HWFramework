package android.view.accessibility;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Log;
import android.view.IWindow;
import android.view.accessibility.IAccessibilityManagerClient.Stub;
import com.android.internal.telephony.RILConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class AccessibilityManager {
    public static final int AUTOCLICK_DELAY_DEFAULT = 600;
    public static final int DALTONIZER_CORRECT_DEUTERANOMALY = 12;
    public static final int DALTONIZER_DISABLED = -1;
    public static final int DALTONIZER_SIMULATE_MONOCHROMACY = 0;
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = "AccessibilityManager";
    public static final int STATE_FLAG_ACCESSIBILITY_ENABLED = 1;
    public static final int STATE_FLAG_HIGH_TEXT_CONTRAST_ENABLED = 4;
    public static final int STATE_FLAG_TOUCH_EXPLORATION_ENABLED = 2;
    private static AccessibilityManager sInstance;
    static final Object sInstanceSync = null;
    private final CopyOnWriteArrayList<AccessibilityStateChangeListener> mAccessibilityStateChangeListeners;
    private final Stub mClient;
    final Handler mHandler;
    private final CopyOnWriteArrayList<HighTextContrastChangeListener> mHighTextContrastStateChangeListeners;
    boolean mIsEnabled;
    boolean mIsHighTextContrastEnabled;
    boolean mIsTouchExplorationEnabled;
    private final Object mLock;
    private IAccessibilityManager mService;
    private final CopyOnWriteArrayList<TouchExplorationStateChangeListener> mTouchExplorationStateChangeListeners;
    final int mUserId;

    public interface AccessibilityStateChangeListener {
        void onAccessibilityStateChanged(boolean z);
    }

    public interface HighTextContrastChangeListener {
        void onHighTextContrastStateChanged(boolean z);
    }

    private final class MyHandler extends Handler {
        public static final int MSG_NOTIFY_ACCESSIBILITY_STATE_CHANGED = 1;
        public static final int MSG_NOTIFY_EXPLORATION_STATE_CHANGED = 2;
        public static final int MSG_NOTIFY_HIGH_TEXT_CONTRAST_STATE_CHANGED = 3;
        public static final int MSG_SET_STATE = 4;

        public MyHandler(Looper looper) {
            super(looper, null, AccessibilityManager.DEBUG);
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case MSG_NOTIFY_ACCESSIBILITY_STATE_CHANGED /*1*/:
                    AccessibilityManager.this.handleNotifyAccessibilityStateChanged();
                case MSG_NOTIFY_EXPLORATION_STATE_CHANGED /*2*/:
                    AccessibilityManager.this.handleNotifyTouchExplorationStateChanged();
                case MSG_NOTIFY_HIGH_TEXT_CONTRAST_STATE_CHANGED /*3*/:
                    AccessibilityManager.this.handleNotifyHighTextContrastStateChanged();
                case MSG_SET_STATE /*4*/:
                    int state = message.arg1;
                    synchronized (AccessibilityManager.this.mLock) {
                        AccessibilityManager.this.setStateLocked(state);
                        break;
                    }
                default:
            }
        }
    }

    public interface TouchExplorationStateChangeListener {
        void onTouchExplorationStateChanged(boolean z);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.accessibility.AccessibilityManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.accessibility.AccessibilityManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.accessibility.AccessibilityManager.<clinit>():void");
    }

    public static AccessibilityManager getInstance(Context context) {
        synchronized (sInstanceSync) {
            if (sInstance == null) {
                int userId;
                if (!(Binder.getCallingUid() == RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED || context.checkCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS") == 0)) {
                    if (context.checkCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL") != 0) {
                        userId = UserHandle.myUserId();
                        sInstance = new AccessibilityManager(context, null, userId);
                    }
                }
                userId = -2;
                sInstance = new AccessibilityManager(context, null, userId);
            }
        }
        return sInstance;
    }

    public AccessibilityManager(Context context, IAccessibilityManager service, int userId) {
        this.mLock = new Object();
        this.mAccessibilityStateChangeListeners = new CopyOnWriteArrayList();
        this.mTouchExplorationStateChangeListeners = new CopyOnWriteArrayList();
        this.mHighTextContrastStateChangeListeners = new CopyOnWriteArrayList();
        this.mClient = new Stub() {
            public void setState(int state) {
                AccessibilityManager.this.mHandler.obtainMessage(AccessibilityManager.STATE_FLAG_HIGH_TEXT_CONTRAST_ENABLED, state, AccessibilityManager.DALTONIZER_SIMULATE_MONOCHROMACY).sendToTarget();
            }
        };
        this.mHandler = new MyHandler(context.getMainLooper());
        this.mUserId = userId;
        synchronized (this.mLock) {
            tryConnectToServiceLocked(service);
        }
    }

    public IAccessibilityManagerClient getClient() {
        return this.mClient;
    }

    public boolean isEnabled() {
        synchronized (this.mLock) {
            if (getServiceLocked() == null) {
                return DEBUG;
            }
            boolean z = this.mIsEnabled;
            return z;
        }
    }

    public boolean isTouchExplorationEnabled() {
        synchronized (this.mLock) {
            if (getServiceLocked() == null) {
                return DEBUG;
            }
            boolean z = this.mIsTouchExplorationEnabled;
            return z;
        }
    }

    public boolean isHighTextContrastEnabled() {
        synchronized (this.mLock) {
            if (getServiceLocked() == null) {
                return DEBUG;
            }
            boolean z = this.mIsHighTextContrastEnabled;
            return z;
        }
    }

    public void sendAccessibilityEvent(AccessibilityEvent event) {
        synchronized (this.mLock) {
            IAccessibilityManager service = getServiceLocked();
            if (service == null) {
            } else if (this.mIsEnabled) {
                int userId = this.mUserId;
                boolean z = DEBUG;
                try {
                    event.setEventTime(SystemClock.uptimeMillis());
                    long identityToken = Binder.clearCallingIdentity();
                    z = service.sendAccessibilityEvent(event, userId);
                    Binder.restoreCallingIdentity(identityToken);
                    if (z) {
                        event.recycle();
                    }
                } catch (RemoteException re) {
                    Log.e(LOG_TAG, "Error during sending " + event + " ", re);
                    if (z) {
                        event.recycle();
                    }
                } catch (Throwable th) {
                    if (z) {
                        event.recycle();
                    }
                }
            } else {
                throw new IllegalStateException("Accessibility off. Did you forget to check that?");
            }
        }
    }

    public void interrupt() {
        synchronized (this.mLock) {
            IAccessibilityManager service = getServiceLocked();
            if (service == null) {
            } else if (this.mIsEnabled) {
                int userId = this.mUserId;
                try {
                    service.interrupt(userId);
                } catch (RemoteException re) {
                    Log.e(LOG_TAG, "Error while requesting interrupt from all services. ", re);
                }
            } else {
                throw new IllegalStateException("Accessibility off. Did you forget to check that?");
            }
        }
    }

    @Deprecated
    public List<ServiceInfo> getAccessibilityServiceList() {
        List<AccessibilityServiceInfo> infos = getInstalledAccessibilityServiceList();
        List<ServiceInfo> services = new ArrayList();
        int infoCount = infos.size();
        for (int i = DALTONIZER_SIMULATE_MONOCHROMACY; i < infoCount; i += STATE_FLAG_ACCESSIBILITY_ENABLED) {
            services.add(((AccessibilityServiceInfo) infos.get(i)).getResolveInfo().serviceInfo);
        }
        return Collections.unmodifiableList(services);
    }

    public List<AccessibilityServiceInfo> getInstalledAccessibilityServiceList() {
        synchronized (this.mLock) {
            IAccessibilityManager service = getServiceLocked();
            if (service == null) {
                List<AccessibilityServiceInfo> emptyList = Collections.emptyList();
                return emptyList;
            }
            int userId = this.mUserId;
            List list = null;
            try {
                list = service.getInstalledAccessibilityServiceList(userId);
            } catch (RemoteException re) {
                Log.e(LOG_TAG, "Error while obtaining the installed AccessibilityServices. ", re);
            }
            if (list != null) {
                return Collections.unmodifiableList(list);
            }
            return Collections.emptyList();
        }
    }

    public List<AccessibilityServiceInfo> getEnabledAccessibilityServiceList(int feedbackTypeFlags) {
        synchronized (this.mLock) {
            IAccessibilityManager service = getServiceLocked();
            if (service == null) {
                List<AccessibilityServiceInfo> emptyList = Collections.emptyList();
                return emptyList;
            }
            int userId = this.mUserId;
            List list = null;
            try {
                list = service.getEnabledAccessibilityServiceList(feedbackTypeFlags, userId);
            } catch (RemoteException re) {
                Log.e(LOG_TAG, "Error while obtaining the installed AccessibilityServices. ", re);
            }
            if (list != null) {
                return Collections.unmodifiableList(list);
            }
            return Collections.emptyList();
        }
    }

    public boolean addAccessibilityStateChangeListener(AccessibilityStateChangeListener listener) {
        return this.mAccessibilityStateChangeListeners.add(listener);
    }

    public boolean removeAccessibilityStateChangeListener(AccessibilityStateChangeListener listener) {
        return this.mAccessibilityStateChangeListeners.remove(listener);
    }

    public boolean addTouchExplorationStateChangeListener(TouchExplorationStateChangeListener listener) {
        return this.mTouchExplorationStateChangeListeners.add(listener);
    }

    public boolean removeTouchExplorationStateChangeListener(TouchExplorationStateChangeListener listener) {
        return this.mTouchExplorationStateChangeListeners.remove(listener);
    }

    public boolean addHighTextContrastStateChangeListener(HighTextContrastChangeListener listener) {
        return this.mHighTextContrastStateChangeListeners.add(listener);
    }

    public boolean removeHighTextContrastStateChangeListener(HighTextContrastChangeListener listener) {
        return this.mHighTextContrastStateChangeListeners.remove(listener);
    }

    private void setStateLocked(int stateFlags) {
        boolean enabled = (stateFlags & STATE_FLAG_ACCESSIBILITY_ENABLED) != 0 ? true : DEBUG;
        boolean touchExplorationEnabled = (stateFlags & STATE_FLAG_TOUCH_EXPLORATION_ENABLED) != 0 ? true : DEBUG;
        boolean highTextContrastEnabled = (stateFlags & STATE_FLAG_HIGH_TEXT_CONTRAST_ENABLED) != 0 ? true : DEBUG;
        boolean wasEnabled = this.mIsEnabled;
        boolean wasTouchExplorationEnabled = this.mIsTouchExplorationEnabled;
        boolean wasHighTextContrastEnabled = this.mIsHighTextContrastEnabled;
        this.mIsEnabled = enabled;
        this.mIsTouchExplorationEnabled = touchExplorationEnabled;
        this.mIsHighTextContrastEnabled = highTextContrastEnabled;
        if (wasEnabled != enabled) {
            this.mHandler.sendEmptyMessage(STATE_FLAG_ACCESSIBILITY_ENABLED);
        }
        if (wasTouchExplorationEnabled != touchExplorationEnabled) {
            this.mHandler.sendEmptyMessage(STATE_FLAG_TOUCH_EXPLORATION_ENABLED);
        }
        if (wasHighTextContrastEnabled != highTextContrastEnabled) {
            this.mHandler.sendEmptyMessage(3);
        }
    }

    public int addAccessibilityInteractionConnection(IWindow windowToken, IAccessibilityInteractionConnection connection) {
        synchronized (this.mLock) {
            IAccessibilityManager service = getServiceLocked();
            if (service == null) {
                return DALTONIZER_DISABLED;
            }
            int userId = this.mUserId;
            try {
                return service.addAccessibilityInteractionConnection(windowToken, connection, userId);
            } catch (RemoteException re) {
                Log.e(LOG_TAG, "Error while adding an accessibility interaction connection. ", re);
                return DALTONIZER_DISABLED;
            }
        }
    }

    public void removeAccessibilityInteractionConnection(IWindow windowToken) {
        synchronized (this.mLock) {
            IAccessibilityManager service = getServiceLocked();
            if (service == null) {
                return;
            }
            try {
                service.removeAccessibilityInteractionConnection(windowToken);
            } catch (RemoteException re) {
                Log.e(LOG_TAG, "Error while removing an accessibility interaction connection. ", re);
            }
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
            IBinder iBinder = ServiceManager.getService("accessibility");
            if (iBinder != null) {
                service = IAccessibilityManager.Stub.asInterface(iBinder);
            } else {
                return;
            }
        }
        try {
            setStateLocked(service.addClient(this.mClient, this.mUserId));
            this.mService = service;
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "AccessibilityManagerService is dead", re);
        }
    }

    private void handleNotifyAccessibilityStateChanged() {
        synchronized (this.mLock) {
            boolean isEnabled = this.mIsEnabled;
        }
        for (AccessibilityStateChangeListener listener : this.mAccessibilityStateChangeListeners) {
            listener.onAccessibilityStateChanged(isEnabled);
        }
    }

    private void handleNotifyTouchExplorationStateChanged() {
        synchronized (this.mLock) {
            boolean isTouchExplorationEnabled = this.mIsTouchExplorationEnabled;
        }
        for (TouchExplorationStateChangeListener listener : this.mTouchExplorationStateChangeListeners) {
            listener.onTouchExplorationStateChanged(isTouchExplorationEnabled);
        }
    }

    private void handleNotifyHighTextContrastStateChanged() {
        synchronized (this.mLock) {
            boolean isHighTextContrastEnabled = this.mIsHighTextContrastEnabled;
        }
        for (HighTextContrastChangeListener listener : this.mHighTextContrastStateChangeListeners) {
            listener.onHighTextContrastStateChanged(isHighTextContrastEnabled);
        }
    }
}
