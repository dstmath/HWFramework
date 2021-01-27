package com.android.server.dreams;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.database.ContentObserver;
import android.hardware.display.AmbientDisplayConfiguration;
import android.hardware.input.InputManagerInternal;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.PowerManagerInternal;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.service.dreams.DreamManagerInternal;
import android.service.dreams.IDreamManager;
import android.util.Slog;
import android.view.Display;
import com.android.internal.util.DumpUtils;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.dreams.DreamController;
import com.android.server.power.ShutdownThread;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DreamManagerService extends SystemService {
    private static final boolean DEBUG = false;
    private static final String TAG = "DreamManagerService";
    private final Context mContext;
    private final DreamController mController;
    private final DreamController.Listener mControllerListener = new DreamController.Listener() {
        /* class com.android.server.dreams.DreamManagerService.AnonymousClass4 */

        @Override // com.android.server.dreams.DreamController.Listener
        public void onDreamStopped(Binder token) {
            synchronized (DreamManagerService.this.mLock) {
                if (DreamManagerService.this.mCurrentDreamToken == token) {
                    DreamManagerService.this.cleanupDreamLocked();
                }
            }
        }
    };
    private boolean mCurrentDreamCanDoze;
    private int mCurrentDreamDozeScreenBrightness = -1;
    private int mCurrentDreamDozeScreenState = 0;
    private boolean mCurrentDreamIsDozing;
    private boolean mCurrentDreamIsTest;
    private boolean mCurrentDreamIsWaking;
    private ComponentName mCurrentDreamName;
    private Binder mCurrentDreamToken;
    private int mCurrentDreamUserId;
    private AmbientDisplayConfiguration mDozeConfig;
    private final ContentObserver mDozeEnabledObserver = new ContentObserver(null) {
        /* class com.android.server.dreams.DreamManagerService.AnonymousClass5 */

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            DreamManagerService.this.writePulseGestureEnabled();
        }
    };
    private final PowerManager.WakeLock mDozeWakeLock;
    private boolean mForceAmbientDisplayEnabled;
    private final DreamHandler mHandler;
    private final Object mLock = new Object();
    private final PowerManager mPowerManager;
    private final PowerManagerInternal mPowerManagerInternal;
    private final Runnable mSystemPropertiesChanged = new Runnable() {
        /* class com.android.server.dreams.DreamManagerService.AnonymousClass6 */

        @Override // java.lang.Runnable
        public void run() {
            synchronized (DreamManagerService.this.mLock) {
                if (DreamManagerService.this.mCurrentDreamName != null && DreamManagerService.this.mCurrentDreamCanDoze && !DreamManagerService.this.mCurrentDreamName.equals(DreamManagerService.this.getDozeComponent())) {
                    DreamManagerService.this.mPowerManager.wakeUp(SystemClock.uptimeMillis(), "android.server.dreams:SYSPROP");
                }
            }
        }
    };

    public DreamManagerService(Context context) {
        super(context);
        this.mContext = context;
        HandlerThread dreamManagerServiceThread = new HandlerThread("Dream_Manager_Service_Thread", 0);
        dreamManagerServiceThread.start();
        this.mHandler = new DreamHandler(dreamManagerServiceThread.getLooper());
        this.mController = new DreamController(context, this.mHandler, this.mControllerListener);
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mPowerManagerInternal = (PowerManagerInternal) getLocalService(PowerManagerInternal.class);
        this.mDozeWakeLock = this.mPowerManager.newWakeLock(64, TAG);
        this.mDozeConfig = new AmbientDisplayConfiguration(this.mContext);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: com.android.server.dreams.DreamManagerService */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.dreams.DreamManagerService$BinderService, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // com.android.server.SystemService
    public void onStart() {
        publishBinderService("dreams", new BinderService());
        publishLocalService(DreamManagerInternal.class, new LocalService());
    }

    @Override // com.android.server.SystemService
    public void onBootPhase(int phase) {
        if (phase == 600) {
            if (Build.IS_DEBUGGABLE) {
                SystemProperties.addChangeCallback(this.mSystemPropertiesChanged);
            }
            this.mContext.registerReceiver(new BroadcastReceiver() {
                /* class com.android.server.dreams.DreamManagerService.AnonymousClass1 */

                @Override // android.content.BroadcastReceiver
                public void onReceive(Context context, Intent intent) {
                    DreamManagerService.this.writePulseGestureEnabled();
                    synchronized (DreamManagerService.this.mLock) {
                        DreamManagerService.this.stopDreamLocked(false);
                    }
                }
            }, new IntentFilter("android.intent.action.USER_SWITCHED"), null, this.mHandler);
            this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("doze_pulse_on_double_tap"), false, this.mDozeEnabledObserver, -1);
            writePulseGestureEnabled();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dumpInternal(PrintWriter pw) {
        pw.println("DREAM MANAGER (dumpsys dreams)");
        pw.println();
        pw.println("mCurrentDreamToken=" + this.mCurrentDreamToken);
        pw.println("mCurrentDreamName=" + this.mCurrentDreamName);
        pw.println("mCurrentDreamUserId=" + this.mCurrentDreamUserId);
        pw.println("mCurrentDreamIsTest=" + this.mCurrentDreamIsTest);
        pw.println("mCurrentDreamCanDoze=" + this.mCurrentDreamCanDoze);
        pw.println("mCurrentDreamIsDozing=" + this.mCurrentDreamIsDozing);
        pw.println("mCurrentDreamIsWaking=" + this.mCurrentDreamIsWaking);
        pw.println("mForceAmbientDisplayEnabled=" + this.mForceAmbientDisplayEnabled);
        pw.println("mCurrentDreamDozeScreenState=" + Display.stateToString(this.mCurrentDreamDozeScreenState));
        pw.println("mCurrentDreamDozeScreenBrightness=" + this.mCurrentDreamDozeScreenBrightness);
        pw.println("getDozeComponent()=" + getDozeComponent());
        pw.println();
        DumpUtils.dumpAsync(this.mHandler, new DumpUtils.Dump() {
            /* class com.android.server.dreams.DreamManagerService.AnonymousClass2 */

            public void dump(PrintWriter pw, String prefix) {
                DreamManagerService.this.mController.dump(pw);
            }
        }, pw, "", 200);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isDreamingInternal() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mCurrentDreamToken != null && !this.mCurrentDreamIsTest && !this.mCurrentDreamIsWaking;
        }
        return z;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void requestDreamInternal() {
        long time = SystemClock.uptimeMillis();
        this.mPowerManager.userActivity(time, true);
        this.mPowerManager.nap(time);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void requestAwakenInternal() {
        this.mPowerManager.userActivity(SystemClock.uptimeMillis(), false);
        stopDreamInternal(false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void finishSelfInternal(IBinder token, boolean immediate) {
        synchronized (this.mLock) {
            if (this.mCurrentDreamToken == token) {
                stopDreamLocked(immediate);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void testDreamInternal(ComponentName dream, int userId) {
        synchronized (this.mLock) {
            startDreamLocked(dream, true, false, userId);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startDreamInternal(boolean doze) {
        int userId = ActivityManager.getCurrentUser();
        ComponentName dream = chooseDreamForUser(doze, userId);
        if (dream != null) {
            synchronized (this.mLock) {
                startDreamLocked(dream, false, doze, userId);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopDreamInternal(boolean immediate) {
        synchronized (this.mLock) {
            stopDreamLocked(immediate);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startDozingInternal(IBinder token, int screenState, int screenBrightness) {
        synchronized (this.mLock) {
            if (this.mCurrentDreamToken == token && this.mCurrentDreamCanDoze) {
                this.mCurrentDreamDozeScreenState = screenState;
                this.mCurrentDreamDozeScreenBrightness = screenBrightness;
                this.mPowerManagerInternal.setDozeOverrideFromDreamManager(screenState, screenBrightness);
                if (!this.mCurrentDreamIsDozing) {
                    this.mCurrentDreamIsDozing = true;
                    this.mDozeWakeLock.acquire();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopDozingInternal(IBinder token) {
        synchronized (this.mLock) {
            if (this.mCurrentDreamToken == token && this.mCurrentDreamIsDozing) {
                this.mCurrentDreamIsDozing = false;
                this.mDozeWakeLock.release();
                this.mPowerManagerInternal.setDozeOverrideFromDreamManager(0, -1);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void forceAmbientDisplayEnabledInternal(boolean enabled) {
        synchronized (this.mLock) {
            this.mForceAmbientDisplayEnabled = enabled;
        }
    }

    private ComponentName chooseDreamForUser(boolean doze, int userId) {
        if (doze) {
            ComponentName dozeComponent = getDozeComponent(userId);
            if (validateDream(dozeComponent)) {
                return dozeComponent;
            }
            return null;
        }
        ComponentName[] dreams = getDreamComponentsForUser(userId);
        if (dreams == null || dreams.length == 0) {
            return null;
        }
        return dreams[0];
    }

    private boolean validateDream(ComponentName component) {
        if (component == null) {
            return false;
        }
        ServiceInfo serviceInfo = getServiceInfo(component);
        if (serviceInfo == null) {
            Slog.w(TAG, "Dream " + component + " does not exist");
            return false;
        } else if (serviceInfo.applicationInfo.targetSdkVersion < 21 || "android.permission.BIND_DREAM_SERVICE".equals(serviceInfo.permission)) {
            return true;
        } else {
            Slog.w(TAG, "Dream " + component + " is not available because its manifest is missing the android.permission.BIND_DREAM_SERVICE permission on the dream service declaration.");
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ComponentName[] getDreamComponentsForUser(int userId) {
        ComponentName defaultDream;
        ComponentName[] components = componentsFromString(Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "screensaver_components", userId));
        List<ComponentName> validComponents = new ArrayList<>();
        if (components != null) {
            for (ComponentName component : components) {
                if (validateDream(component)) {
                    validComponents.add(component);
                }
            }
        }
        if (validComponents.isEmpty() && (defaultDream = getDefaultDreamComponentForUser(userId)) != null) {
            Slog.w(TAG, "Falling back to default dream " + defaultDream);
            validComponents.add(defaultDream);
        }
        return (ComponentName[]) validComponents.toArray(new ComponentName[validComponents.size()]);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setDreamComponentsForUser(int userId, ComponentName[] componentNames) {
        Settings.Secure.putStringForUser(this.mContext.getContentResolver(), "screensaver_components", componentsToString(componentNames), userId);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ComponentName getDefaultDreamComponentForUser(int userId) {
        String name = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "screensaver_default_component", userId);
        if (name == null) {
            return null;
        }
        return ComponentName.unflattenFromString(name);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ComponentName getDozeComponent() {
        return getDozeComponent(ActivityManager.getCurrentUser());
    }

    private ComponentName getDozeComponent(int userId) {
        if (this.mForceAmbientDisplayEnabled || this.mDozeConfig.enabled(userId)) {
            return ComponentName.unflattenFromString(this.mDozeConfig.ambientDisplayComponent());
        }
        return null;
    }

    private ServiceInfo getServiceInfo(ComponentName name) {
        if (name == null) {
            return null;
        }
        try {
            return this.mContext.getPackageManager().getServiceInfo(name, 268435456);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private void startDreamLocked(ComponentName name, boolean isTest, boolean canDoze, int userId) {
        if (Objects.equals(this.mCurrentDreamName, name) && this.mCurrentDreamIsTest == isTest && this.mCurrentDreamCanDoze == canDoze && this.mCurrentDreamUserId == userId) {
            Slog.i(TAG, "Already in target dream.");
        } else if (!isInSuperWallpaperShutdown()) {
            stopDreamLocked(true);
            Slog.i(TAG, "Entering dreamland.");
            Binder newToken = new Binder();
            this.mCurrentDreamToken = newToken;
            this.mCurrentDreamName = name;
            this.mCurrentDreamIsTest = isTest;
            this.mCurrentDreamCanDoze = canDoze;
            this.mCurrentDreamUserId = userId;
            PowerManager.WakeLock wakeLock = this.mPowerManager.newWakeLock(1, "startDream");
            this.mHandler.post(wakeLock.wrap(new Runnable(newToken, name, isTest, canDoze, userId, wakeLock) {
                /* class com.android.server.dreams.$$Lambda$DreamManagerService$f7cEVKQvPKMm_Ir9dq0e6PSOkX8 */
                private final /* synthetic */ Binder f$1;
                private final /* synthetic */ ComponentName f$2;
                private final /* synthetic */ boolean f$3;
                private final /* synthetic */ boolean f$4;
                private final /* synthetic */ int f$5;
                private final /* synthetic */ PowerManager.WakeLock f$6;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                    this.f$5 = r6;
                    this.f$6 = r7;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    DreamManagerService.this.lambda$startDreamLocked$0$DreamManagerService(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6);
                }
            }));
        }
    }

    public /* synthetic */ void lambda$startDreamLocked$0$DreamManagerService(Binder newToken, ComponentName name, boolean isTest, boolean canDoze, int userId, PowerManager.WakeLock wakeLock) {
        this.mController.startDream(newToken, name, isTest, canDoze, userId, wakeLock);
    }

    private boolean isInSuperWallpaperShutdown() {
        String shutdownAct;
        if (!(Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "in_wallpaper_effect", 0, -2) == 1) || (shutdownAct = SystemProperties.get(ShutdownThread.SHUTDOWN_ACTION_PROPERTY, "")) == null || shutdownAct.length() <= 0) {
            return false;
        }
        Slog.i(TAG, "ignore startDreamLocked as isSuperWallpaperEffect in shutdownAct " + shutdownAct);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopDreamLocked(final boolean immediate) {
        if (this.mCurrentDreamToken != null) {
            if (immediate) {
                Slog.i(TAG, "Leaving dreamland.");
                cleanupDreamLocked();
            } else if (!this.mCurrentDreamIsWaking) {
                Slog.i(TAG, "Gently waking up from dream.");
                this.mCurrentDreamIsWaking = true;
            } else {
                return;
            }
            this.mHandler.post(new Runnable() {
                /* class com.android.server.dreams.DreamManagerService.AnonymousClass3 */

                @Override // java.lang.Runnable
                public void run() {
                    Slog.i(DreamManagerService.TAG, "Performing gentle wake from dream.");
                    DreamManagerService.this.mController.stopDream(immediate);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cleanupDreamLocked() {
        this.mCurrentDreamToken = null;
        this.mCurrentDreamName = null;
        this.mCurrentDreamIsTest = false;
        this.mCurrentDreamCanDoze = false;
        this.mCurrentDreamUserId = 0;
        this.mCurrentDreamIsWaking = false;
        if (this.mCurrentDreamIsDozing) {
            this.mCurrentDreamIsDozing = false;
            this.mDozeWakeLock.release();
        }
        this.mCurrentDreamDozeScreenState = 0;
        this.mCurrentDreamDozeScreenBrightness = -1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkPermission(String permission) {
        if (this.mContext.checkCallingOrSelfPermission(permission) != 0) {
            throw new SecurityException("Access denied to process: " + Binder.getCallingPid() + ", must have permission " + permission);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void writePulseGestureEnabled() {
        ((InputManagerInternal) LocalServices.getService(InputManagerInternal.class)).setPulseGestureEnabled(validateDream(getDozeComponent()));
    }

    private static String componentsToString(ComponentName[] componentNames) {
        StringBuilder names = new StringBuilder();
        if (componentNames != null) {
            for (ComponentName componentName : componentNames) {
                if (names.length() > 0) {
                    names.append(',');
                }
                names.append(componentName.flattenToString());
            }
        }
        return names.toString();
    }

    private static ComponentName[] componentsFromString(String names) {
        if (names == null) {
            return null;
        }
        String[] namesArray = names.split(",");
        ComponentName[] componentNames = new ComponentName[namesArray.length];
        for (int i = 0; i < namesArray.length; i++) {
            componentNames[i] = ComponentName.unflattenFromString(namesArray[i]);
        }
        return componentNames;
    }

    /* access modifiers changed from: private */
    public final class DreamHandler extends Handler {
        public DreamHandler(Looper looper) {
            super(looper, null, true);
        }
    }

    private final class BinderService extends IDreamManager.Stub {
        private BinderService() {
        }

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpPermission(DreamManagerService.this.mContext, DreamManagerService.TAG, pw)) {
                long ident = Binder.clearCallingIdentity();
                try {
                    DreamManagerService.this.dumpInternal(pw);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }

        public ComponentName[] getDreamComponents() {
            DreamManagerService.this.checkPermission("android.permission.READ_DREAM_STATE");
            int userId = UserHandle.getCallingUserId();
            long ident = Binder.clearCallingIdentity();
            try {
                return DreamManagerService.this.getDreamComponentsForUser(userId);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void setDreamComponents(ComponentName[] componentNames) {
            DreamManagerService.this.checkPermission("android.permission.WRITE_DREAM_STATE");
            int userId = UserHandle.getCallingUserId();
            long ident = Binder.clearCallingIdentity();
            try {
                DreamManagerService.this.setDreamComponentsForUser(userId, componentNames);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public ComponentName getDefaultDreamComponent() {
            DreamManagerService.this.checkPermission("android.permission.READ_DREAM_STATE");
            int userId = UserHandle.getCallingUserId();
            long ident = Binder.clearCallingIdentity();
            try {
                return DreamManagerService.this.getDefaultDreamComponentForUser(userId);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean isDreaming() {
            DreamManagerService.this.checkPermission("android.permission.READ_DREAM_STATE");
            long ident = Binder.clearCallingIdentity();
            try {
                return DreamManagerService.this.isDreamingInternal();
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void dream() {
            DreamManagerService.this.checkPermission("android.permission.WRITE_DREAM_STATE");
            long ident = Binder.clearCallingIdentity();
            try {
                DreamManagerService.this.requestDreamInternal();
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void testDream(ComponentName dream) {
            if (dream != null) {
                DreamManagerService.this.checkPermission("android.permission.WRITE_DREAM_STATE");
                int callingUserId = UserHandle.getCallingUserId();
                int currentUserId = ActivityManager.getCurrentUser();
                if (callingUserId != currentUserId) {
                    Slog.w(DreamManagerService.TAG, "Aborted attempt to start a test dream while a different  user is active: callingUserId=" + callingUserId + ", currentUserId=" + currentUserId);
                    return;
                }
                long ident = Binder.clearCallingIdentity();
                try {
                    DreamManagerService.this.testDreamInternal(dream, callingUserId);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                throw new IllegalArgumentException("dream must not be null");
            }
        }

        public void awaken() {
            DreamManagerService.this.checkPermission("android.permission.WRITE_DREAM_STATE");
            long ident = Binder.clearCallingIdentity();
            try {
                DreamManagerService.this.requestAwakenInternal();
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void finishSelf(IBinder token, boolean immediate) {
            if (token != null) {
                long ident = Binder.clearCallingIdentity();
                try {
                    DreamManagerService.this.finishSelfInternal(token, immediate);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                throw new IllegalArgumentException("token must not be null");
            }
        }

        public void startDozing(IBinder token, int screenState, int screenBrightness) {
            if (token != null) {
                long ident = Binder.clearCallingIdentity();
                try {
                    DreamManagerService.this.startDozingInternal(token, screenState, screenBrightness);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                throw new IllegalArgumentException("token must not be null");
            }
        }

        public void stopDozing(IBinder token) {
            if (token != null) {
                long ident = Binder.clearCallingIdentity();
                try {
                    DreamManagerService.this.stopDozingInternal(token);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                throw new IllegalArgumentException("token must not be null");
            }
        }

        public void forceAmbientDisplayEnabled(boolean enabled) {
            DreamManagerService.this.checkPermission("android.permission.DEVICE_POWER");
            long ident = Binder.clearCallingIdentity();
            try {
                DreamManagerService.this.forceAmbientDisplayEnabledInternal(enabled);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    private final class LocalService extends DreamManagerInternal {
        private LocalService() {
        }

        public void startDream(boolean doze) {
            DreamManagerService.this.startDreamInternal(doze);
        }

        public void stopDream(boolean immediate) {
            DreamManagerService.this.stopDreamInternal(immediate);
        }

        public boolean isDreaming() {
            return DreamManagerService.this.isDreamingInternal();
        }
    }
}
