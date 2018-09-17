package com.android.server.dreams;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.PowerManagerInternal;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.service.dreams.DreamManagerInternal;
import android.service.dreams.IDreamManager.Stub;
import android.text.TextUtils;
import android.util.Slog;
import android.view.Display;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.DumpUtils.Dump;
import com.android.server.FgThread;
import com.android.server.SystemService;
import com.android.server.dreams.DreamController.Listener;
import huawei.cust.HwCustUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import libcore.util.Objects;

public final class DreamManagerService extends SystemService {
    private static final boolean DEBUG = false;
    private static final String TAG = "DreamManagerService";
    private final Context mContext;
    private final DreamController mController;
    private final Listener mControllerListener;
    private boolean mCurrentDreamCanDoze;
    private int mCurrentDreamDozeScreenBrightness;
    private int mCurrentDreamDozeScreenState;
    private boolean mCurrentDreamIsDozing;
    private boolean mCurrentDreamIsTest;
    private boolean mCurrentDreamIsWaking;
    private ComponentName mCurrentDreamName;
    private Binder mCurrentDreamToken;
    private int mCurrentDreamUserId;
    private HwCustDreamManagerService mCust;
    private final WakeLock mDozeWakeLock;
    private final DreamHandler mHandler;
    private final Object mLock;
    private final PowerManager mPowerManager;
    private final PowerManagerInternal mPowerManagerInternal;
    private final Runnable mSystemPropertiesChanged;

    /* renamed from: com.android.server.dreams.DreamManagerService.5 */
    class AnonymousClass5 implements Runnable {
        final /* synthetic */ boolean val$canDoze;
        final /* synthetic */ boolean val$isTest;
        final /* synthetic */ ComponentName val$name;
        final /* synthetic */ Binder val$newToken;
        final /* synthetic */ int val$userId;

        AnonymousClass5(Binder val$newToken, ComponentName val$name, boolean val$isTest, int val$userId, boolean val$canDoze) {
            this.val$newToken = val$newToken;
            this.val$name = val$name;
            this.val$isTest = val$isTest;
            this.val$userId = val$userId;
            this.val$canDoze = val$canDoze;
        }

        public void run() {
            if (DreamManagerService.this.mCust == null || !DreamManagerService.this.mCust.isChargingAlbumEnabled()) {
                DreamManagerService.this.mController.startDream(this.val$newToken, this.val$name, this.val$isTest, this.val$canDoze, this.val$userId);
            } else if (DreamManagerService.this.mController.getCust() != null) {
                DreamManagerService.this.mController.getCust().startChargingAlbumDream(this.val$newToken, this.val$name, this.val$isTest, this.val$userId);
            }
        }
    }

    /* renamed from: com.android.server.dreams.DreamManagerService.6 */
    class AnonymousClass6 implements Runnable {
        final /* synthetic */ boolean val$immediate;

        AnonymousClass6(boolean val$immediate) {
            this.val$immediate = val$immediate;
        }

        public void run() {
            DreamManagerService.this.mController.stopDream(this.val$immediate);
        }
    }

    private final class BinderService extends Stub {
        private BinderService() {
        }

        protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DreamManagerService.this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
                pw.println("Permission Denial: can't dump DreamManager from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                return;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                DreamManagerService.this.dumpInternal(pw);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean isChargingAlbumEnabled() {
            DreamManagerService.this.checkPermission("android.permission.READ_DREAM_STATE");
            if (DreamManagerService.this.mCust != null) {
                return DreamManagerService.this.mCust.isChargingAlbumEnabled();
            }
            return DreamManagerService.DEBUG;
        }

        public ComponentName[] getDreamComponents() {
            DreamManagerService.this.checkPermission("android.permission.READ_DREAM_STATE");
            int userId = UserHandle.getCallingUserId();
            long ident = Binder.clearCallingIdentity();
            try {
                ComponentName[] -wrap0 = DreamManagerService.this.getDreamComponentsForUser(userId);
                return -wrap0;
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
                ComponentName -wrap1 = DreamManagerService.this.getDefaultDreamComponentForUser(userId);
                return -wrap1;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean isDreaming() {
            DreamManagerService.this.checkPermission("android.permission.READ_DREAM_STATE");
            long ident = Binder.clearCallingIdentity();
            try {
                boolean -wrap3 = DreamManagerService.this.isDreamingInternal();
                return -wrap3;
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
            if (dream == null) {
                throw new IllegalArgumentException("dream must not be null");
            }
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
            if (token == null) {
                throw new IllegalArgumentException("token must not be null");
            }
            long ident = Binder.clearCallingIdentity();
            try {
                DreamManagerService.this.finishSelfInternal(token, immediate);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void startDozing(IBinder token, int screenState, int screenBrightness) {
            if (token == null) {
                throw new IllegalArgumentException("token must not be null");
            }
            long ident = Binder.clearCallingIdentity();
            try {
                DreamManagerService.this.startDozingInternal(token, screenState, screenBrightness);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void stopDozing(IBinder token) {
            if (token == null) {
                throw new IllegalArgumentException("token must not be null");
            }
            long ident = Binder.clearCallingIdentity();
            try {
                DreamManagerService.this.stopDozingInternal(token);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    private final class DreamHandler extends Handler {
        public DreamHandler(Looper looper) {
            super(looper, null, true);
        }
    }

    private final class LocalService extends DreamManagerInternal {
        private LocalService() {
        }

        public void startDream(boolean doze) {
            if (DreamManagerService.this.mCust == null || !DreamManagerService.this.mCust.isChargingAlbumEnabled()) {
                DreamManagerService.this.startDreamInternal(doze);
            } else if (DreamManagerService.this.mCust.isCoverOpened()) {
                DreamManagerService.this.startDreamInternal(doze);
            }
        }

        public void stopDream(boolean immediate) {
            DreamManagerService.this.stopDreamInternal(immediate);
        }

        public boolean isDreaming() {
            return DreamManagerService.this.isDreamingInternal();
        }
    }

    public DreamManagerService(Context context) {
        super(context);
        this.mLock = new Object();
        this.mCurrentDreamDozeScreenState = 0;
        this.mCurrentDreamDozeScreenBrightness = -1;
        this.mControllerListener = new Listener() {
            public void onDreamStopped(Binder token) {
                synchronized (DreamManagerService.this.mLock) {
                    if (DreamManagerService.this.mCurrentDreamToken == token) {
                        DreamManagerService.this.cleanupDreamLocked();
                    }
                }
            }
        };
        this.mSystemPropertiesChanged = new Runnable() {
            public void run() {
                synchronized (DreamManagerService.this.mLock) {
                    if (!(DreamManagerService.this.mCurrentDreamName == null || !DreamManagerService.this.mCurrentDreamCanDoze || DreamManagerService.this.mCurrentDreamName.equals(DreamManagerService.this.getDozeComponent()))) {
                        DreamManagerService.this.mPowerManager.wakeUp(SystemClock.uptimeMillis(), "android.server.dreams:SYSPROP");
                    }
                }
            }
        };
        this.mContext = context;
        this.mHandler = new DreamHandler(FgThread.get().getLooper());
        this.mController = new DreamController(context, this.mHandler, this.mControllerListener);
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mPowerManagerInternal = (PowerManagerInternal) getLocalService(PowerManagerInternal.class);
        this.mDozeWakeLock = this.mPowerManager.newWakeLock(64, TAG);
        this.mCust = (HwCustDreamManagerService) HwCustUtils.createObj(HwCustDreamManagerService.class, new Object[]{this.mContext});
    }

    public void onStart() {
        publishBinderService("dreams", new BinderService());
        publishLocalService(DreamManagerInternal.class, new LocalService());
    }

    public void onBootPhase(int phase) {
        if (phase == NetdResponseCode.InterfaceChange) {
            if (Build.IS_DEBUGGABLE) {
                SystemProperties.addChangeCallback(this.mSystemPropertiesChanged);
            }
            this.mContext.registerReceiver(new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    synchronized (DreamManagerService.this.mLock) {
                        DreamManagerService.this.stopDreamLocked(DreamManagerService.DEBUG);
                    }
                }
            }, new IntentFilter("android.intent.action.USER_SWITCHED"), null, this.mHandler);
        }
    }

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
        pw.println("mCurrentDreamDozeScreenState=" + Display.stateToString(this.mCurrentDreamDozeScreenState));
        pw.println("mCurrentDreamDozeScreenBrightness=" + this.mCurrentDreamDozeScreenBrightness);
        pw.println("getDozeComponent()=" + getDozeComponent());
        pw.println();
        DumpUtils.dumpAsync(this.mHandler, new Dump() {
            public void dump(PrintWriter pw, String prefix) {
                DreamManagerService.this.mController.dump(pw);
            }
        }, pw, "", 200);
    }

    private boolean isDreamingInternal() {
        boolean z = DEBUG;
        synchronized (this.mLock) {
            if (!(this.mCurrentDreamToken == null || this.mCurrentDreamIsTest)) {
                if (!this.mCurrentDreamIsWaking) {
                    z = true;
                }
            }
        }
        return z;
    }

    private void requestDreamInternal() {
        long time = SystemClock.uptimeMillis();
        this.mPowerManager.userActivity(time, true);
        this.mPowerManager.nap(time);
    }

    private void requestAwakenInternal() {
        this.mPowerManager.userActivity(SystemClock.uptimeMillis(), DEBUG);
        stopDreamInternal(DEBUG);
    }

    private void finishSelfInternal(IBinder token, boolean immediate) {
        synchronized (this.mLock) {
            if (this.mCurrentDreamToken == token) {
                stopDreamLocked(immediate);
            }
        }
    }

    private void testDreamInternal(ComponentName dream, int userId) {
        synchronized (this.mLock) {
            startDreamLocked(dream, true, DEBUG, userId);
        }
    }

    private void startDreamInternal(boolean doze) {
        int userId = ActivityManager.getCurrentUser();
        ComponentName dream = chooseDreamForUser(doze, userId);
        if (dream != null) {
            synchronized (this.mLock) {
                startDreamLocked(dream, DEBUG, doze, userId);
            }
        }
    }

    public void systemReady() {
        if (this.mCust != null) {
            this.mCust.systemReady();
        }
    }

    private void stopDreamInternal(boolean immediate) {
        synchronized (this.mLock) {
            stopDreamLocked(immediate);
        }
    }

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

    private void stopDozingInternal(IBinder token) {
        synchronized (this.mLock) {
            if (this.mCurrentDreamToken == token && this.mCurrentDreamIsDozing) {
                this.mCurrentDreamIsDozing = DEBUG;
                this.mDozeWakeLock.release();
                this.mPowerManagerInternal.setDozeOverrideFromDreamManager(0, -1);
            }
        }
    }

    private ComponentName chooseDreamForUser(boolean doze, int userId) {
        ComponentName componentName = null;
        if (doze) {
            ComponentName dozeComponent = getDozeComponent(userId);
            if (!validateDream(dozeComponent)) {
                dozeComponent = null;
            }
            return dozeComponent;
        }
        ComponentName[] dreams;
        if (this.mCust == null || !this.mCust.isChargingAlbumEnabled()) {
            dreams = getDreamComponentsForUser(userId);
        } else {
            dreams = this.mCust.getChargingAlbumForUser(userId);
        }
        if (!(dreams == null || dreams.length == 0)) {
            componentName = dreams[0];
        }
        return componentName;
    }

    private boolean validateDream(ComponentName component) {
        if (component == null) {
            return DEBUG;
        }
        ServiceInfo serviceInfo = getServiceInfo(component);
        if (serviceInfo == null) {
            Slog.w(TAG, "Dream " + component + " does not exist");
            return DEBUG;
        } else if (serviceInfo.applicationInfo.targetSdkVersion < 21 || "android.permission.BIND_DREAM_SERVICE".equals(serviceInfo.permission)) {
            return true;
        } else {
            Slog.w(TAG, "Dream " + component + " is not available because its manifest is missing the " + "android.permission.BIND_DREAM_SERVICE" + " permission on the dream service declaration.");
            return DEBUG;
        }
    }

    private ComponentName[] getDreamComponentsForUser(int userId) {
        ComponentName[] components = componentsFromString(Secure.getStringForUser(this.mContext.getContentResolver(), "screensaver_components", userId));
        List<ComponentName> validComponents = new ArrayList();
        if (components != null) {
            for (ComponentName component : components) {
                if (validateDream(component)) {
                    validComponents.add(component);
                }
            }
        }
        if (validComponents.isEmpty()) {
            ComponentName defaultDream = getDefaultDreamComponentForUser(userId);
            if (defaultDream != null) {
                Slog.w(TAG, "Falling back to default dream " + defaultDream);
                validComponents.add(defaultDream);
            }
        }
        return (ComponentName[]) validComponents.toArray(new ComponentName[validComponents.size()]);
    }

    private void setDreamComponentsForUser(int userId, ComponentName[] componentNames) {
        Secure.putStringForUser(this.mContext.getContentResolver(), "screensaver_components", componentsToString(componentNames), userId);
    }

    private ComponentName getDefaultDreamComponentForUser(int userId) {
        String name = Secure.getStringForUser(this.mContext.getContentResolver(), "screensaver_default_component", userId);
        if (name == null) {
            return null;
        }
        return ComponentName.unflattenFromString(name);
    }

    private ComponentName getDozeComponent() {
        return getDozeComponent(ActivityManager.getCurrentUser());
    }

    private ComponentName getDozeComponent(int userId) {
        String name;
        if (Build.IS_DEBUGGABLE) {
            name = SystemProperties.get("debug.doze.component");
        } else {
            name = null;
        }
        if (TextUtils.isEmpty(name)) {
            name = this.mContext.getResources().getString(17039450);
        }
        boolean enabled = Secure.getIntForUser(this.mContext.getContentResolver(), "doze_enabled", 1, userId) != 0 ? true : DEBUG;
        if (TextUtils.isEmpty(name) || !enabled) {
            return null;
        }
        return ComponentName.unflattenFromString(name);
    }

    private ServiceInfo getServiceInfo(ComponentName name) {
        ServiceInfo serviceInfo = null;
        if (name != null) {
            try {
                serviceInfo = this.mContext.getPackageManager().getServiceInfo(name, 268435456);
            } catch (NameNotFoundException e) {
                return serviceInfo;
            }
        }
        return serviceInfo;
    }

    private void startDreamLocked(ComponentName name, boolean isTest, boolean canDoze, int userId) {
        if (!Objects.equal(this.mCurrentDreamName, name) || this.mCurrentDreamIsTest != isTest || this.mCurrentDreamCanDoze != canDoze || this.mCurrentDreamUserId != userId) {
            stopDreamLocked(true);
            Slog.i(TAG, "Entering dreamland.");
            Binder newToken = new Binder();
            this.mCurrentDreamToken = newToken;
            this.mCurrentDreamName = name;
            this.mCurrentDreamIsTest = isTest;
            this.mCurrentDreamCanDoze = canDoze;
            this.mCurrentDreamUserId = userId;
            this.mHandler.post(new AnonymousClass5(newToken, name, isTest, userId, canDoze));
        }
    }

    private void stopDreamLocked(boolean immediate) {
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
            this.mHandler.post(new AnonymousClass6(immediate));
        }
    }

    private void cleanupDreamLocked() {
        this.mCurrentDreamToken = null;
        this.mCurrentDreamName = null;
        this.mCurrentDreamIsTest = DEBUG;
        this.mCurrentDreamCanDoze = DEBUG;
        this.mCurrentDreamUserId = 0;
        this.mCurrentDreamIsWaking = DEBUG;
        if (this.mCurrentDreamIsDozing) {
            this.mCurrentDreamIsDozing = DEBUG;
            this.mDozeWakeLock.release();
        }
        this.mCurrentDreamDozeScreenState = 0;
        this.mCurrentDreamDozeScreenBrightness = -1;
    }

    private void checkPermission(String permission) {
        if (this.mContext.checkCallingOrSelfPermission(permission) != 0) {
            throw new SecurityException("Access denied to process: " + Binder.getCallingPid() + ", must have permission " + permission);
        }
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
}
