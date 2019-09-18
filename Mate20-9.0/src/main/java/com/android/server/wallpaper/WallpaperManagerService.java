package com.android.server.wallpaper;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.IWallpaperManager;
import android.app.IWallpaperManagerCallback;
import android.app.PendingIntent;
import android.app.UserSwitchObserver;
import android.app.WallpaperColors;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.app.admin.DevicePolicyManager;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.hdm.HwDeviceManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.IRemoteCallback;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SELinux;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.service.wallpaper.IWallpaperConnection;
import android.service.wallpaper.IWallpaperEngine;
import android.service.wallpaper.IWallpaperService;
import android.system.ErrnoException;
import android.system.Os;
import android.util.EventLog;
import android.util.Slog;
import android.util.SparseArray;
import android.util.Xml;
import android.view.IWindowManager;
import android.view.WindowManager;
import android.widget.Toast;
import com.android.internal.content.PackageMonitor;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.JournaledFile;
import com.android.server.BatteryService;
import com.android.server.EventLogTags;
import com.android.server.FgThread;
import com.android.server.SystemService;
import com.android.server.pm.PackageManagerService;
import com.android.server.wallpaper.WallpaperManagerService;
import huawei.android.security.IHwBehaviorCollectManager;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class WallpaperManagerService extends IWallpaperManager.Stub implements IWallpaperManagerService {
    static final boolean DEBUG = false;
    static final boolean DEBUG_LIVE = true;
    static final int MAX_WALLPAPER_COMPONENT_LOG_LENGTH = 128;
    static final long MIN_WALLPAPER_CRASH_TIME = 10000;
    static final String TAG = "WallpaperManagerService";
    static final String WALLPAPER = "wallpaper_orig";
    static final String WALLPAPER_CROP = "wallpaper";
    static final String WALLPAPER_INFO = "wallpaper_info.xml";
    static final String WALLPAPER_LOCK_CROP = "wallpaper_lock";
    static final String WALLPAPER_LOCK_ORIG = "wallpaper_lock_orig";
    public static Handler mHandler = new Handler(Looper.getMainLooper());
    static final String[] sPerUserFiles = {WALLPAPER, WALLPAPER_CROP, WALLPAPER_LOCK_ORIG, WALLPAPER_LOCK_CROP, WALLPAPER_INFO};
    final AppOpsManager mAppOpsManager;
    final SparseArray<RemoteCallbackList<IWallpaperManagerCallback>> mColorsChangedListeners;
    final Context mContext;
    int mCurrentUserId = -10000;
    ComponentName mDefaultWallpaperComponent;
    final IPackageManager mIPackageManager;
    final IWindowManager mIWindowManager;
    final ComponentName mImageWallpaper;
    boolean mInAmbientMode;
    boolean mIsLoadLiveWallpaper = false;
    IWallpaperManagerCallback mKeyguardListener;
    WallpaperData mLastWallpaper;
    final Object mLock = new Object();
    final SparseArray<WallpaperData> mLockWallpaperMap = new SparseArray<>();
    final MyPackageMonitor mMonitor;
    boolean mShuttingDown;
    boolean mSuccess = false;
    int mThemeMode;
    final SparseArray<Boolean> mUserRestorecon = new SparseArray<>();
    boolean mWaitingForUnlock;
    int mWallpaperId;
    final SparseArray<WallpaperData> mWallpaperMap = new SparseArray<>();

    public static class Lifecycle extends SystemService {
        private IWallpaperManagerService mService;

        public Lifecycle(Context context) {
            super(context);
        }

        public void onStart() {
            try {
                this.mService = (IWallpaperManagerService) Class.forName(getContext().getResources().getString(17039846)).getConstructor(new Class[]{Context.class}).newInstance(new Object[]{getContext()});
                publishBinderService(WallpaperManagerService.WALLPAPER_CROP, this.mService);
            } catch (Exception exp) {
                Slog.wtf(WallpaperManagerService.TAG, "Failed to instantiate WallpaperManagerService", exp);
            }
        }

        public void onBootPhase(int phase) {
            if (this.mService != null) {
                this.mService.onBootPhase(phase);
            }
        }

        public void onUnlockUser(int userHandle) {
            if (this.mService != null) {
                this.mService.onUnlockUser(userHandle);
            }
        }
    }

    class MyPackageMonitor extends PackageMonitor {
        static final String PACKAGE_SYSTEMUI = "com.android.systemui";

        MyPackageMonitor() {
        }

        /* JADX WARNING: Code restructure failed: missing block: B:17:0x0085, code lost:
            return;
         */
        public void onPackageUpdateFinished(String packageName, int uid) {
            synchronized (WallpaperManagerService.this.mLock) {
                if (WallpaperManagerService.this.mCurrentUserId == getChangingUserId()) {
                    WallpaperData wallpaper = WallpaperManagerService.this.mWallpaperMap.get(WallpaperManagerService.this.mCurrentUserId);
                    if (wallpaper != null) {
                        ComponentName wpService = wallpaper.wallpaperComponent;
                        if (wpService != null && wpService.getPackageName().equals(packageName)) {
                            Slog.i(WallpaperManagerService.TAG, "Wallpaper " + wpService + " update has finished");
                            wallpaper.wallpaperUpdating = false;
                            WallpaperManagerService.this.clearWallpaperComponentLocked(wallpaper);
                            if (!WallpaperManagerService.this.bindWallpaperComponentLocked(wpService, false, false, wallpaper, null)) {
                                Slog.w(WallpaperManagerService.TAG, "Wallpaper " + wpService + " no longer available; reverting to default");
                                WallpaperManagerService.this.clearWallpaperLocked(false, 1, wallpaper.userId, null);
                            }
                        }
                    }
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0038, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:18:0x003a, code lost:
            return;
         */
        public void onPackageModified(String packageName) {
            synchronized (WallpaperManagerService.this.mLock) {
                if (WallpaperManagerService.this.mCurrentUserId == getChangingUserId()) {
                    WallpaperData wallpaper = WallpaperManagerService.this.mWallpaperMap.get(WallpaperManagerService.this.mCurrentUserId);
                    if (wallpaper != null) {
                        if (wallpaper.wallpaperComponent != null) {
                            if (wallpaper.wallpaperComponent.getPackageName().equals(packageName)) {
                                doPackagesChangedLocked(true, wallpaper);
                            }
                        }
                    }
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:17:0x0063, code lost:
            return;
         */
        public void onPackageUpdateStarted(String packageName, int uid) {
            synchronized (WallpaperManagerService.this.mLock) {
                if (WallpaperManagerService.this.mCurrentUserId == getChangingUserId()) {
                    WallpaperData wallpaper = WallpaperManagerService.this.mWallpaperMap.get(WallpaperManagerService.this.mCurrentUserId);
                    if (!(wallpaper == null || wallpaper.wallpaperComponent == null || !wallpaper.wallpaperComponent.getPackageName().equals(packageName))) {
                        Slog.i(WallpaperManagerService.TAG, "Wallpaper service " + wallpaper.wallpaperComponent + " is updating");
                        wallpaper.wallpaperUpdating = true;
                        if (wallpaper.connection != null) {
                            FgThread.getHandler().removeCallbacks(wallpaper.connection.mResetRunnable);
                        }
                    }
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:18:0x0042, code lost:
            return r1;
         */
        public boolean onHandleForceStop(Intent intent, String[] packages, int uid, boolean doit) {
            synchronized (WallpaperManagerService.this.mLock) {
                boolean changed = false;
                int i = 0;
                if (WallpaperManagerService.this.mCurrentUserId != getChangingUserId()) {
                    return false;
                }
                int length = packages.length;
                while (true) {
                    if (i >= length) {
                        break;
                    } else if (PACKAGE_SYSTEMUI.equals(packages[i])) {
                        doit = false;
                        Slog.w(WallpaperManagerService.TAG, "SystemUI has been forced to stop!");
                        break;
                    } else {
                        i++;
                    }
                }
                WallpaperData wallpaper = WallpaperManagerService.this.mWallpaperMap.get(WallpaperManagerService.this.mCurrentUserId);
                if (wallpaper != null) {
                    changed = false | doPackagesChangedLocked(doit, wallpaper);
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0026, code lost:
            return;
         */
        public void onSomePackagesChanged() {
            synchronized (WallpaperManagerService.this.mLock) {
                if (WallpaperManagerService.this.mCurrentUserId == getChangingUserId()) {
                    WallpaperData wallpaper = WallpaperManagerService.this.mWallpaperMap.get(WallpaperManagerService.this.mCurrentUserId);
                    if (wallpaper != null) {
                        doPackagesChangedLocked(true, wallpaper);
                    }
                }
            }
        }

        /* access modifiers changed from: package-private */
        public boolean doPackagesChangedLocked(boolean doit, WallpaperData wallpaper) {
            boolean changed = false;
            if (wallpaper.wallpaperComponent != null) {
                int change = isPackageDisappearing(wallpaper.wallpaperComponent.getPackageName());
                if (change == 3 || change == 2) {
                    changed = true;
                    if (doit) {
                        Slog.w(WallpaperManagerService.TAG, "Wallpaper uninstalled, removing: " + wallpaper.wallpaperComponent);
                        WallpaperManagerService.this.clearWallpaperLocked(false, 1, wallpaper.userId, null);
                        WallpaperManagerService.this.mContext.sendBroadcastAsUser(new Intent("android.intent.action.WALLPAPER_CHANGED"), new UserHandle(WallpaperManagerService.this.mCurrentUserId));
                    }
                }
            }
            if (wallpaper.nextWallpaperComponent != null) {
                int change2 = isPackageDisappearing(wallpaper.nextWallpaperComponent.getPackageName());
                if (change2 == 3 || change2 == 2) {
                    wallpaper.nextWallpaperComponent = null;
                }
            }
            if (wallpaper.wallpaperComponent != null && isPackageModified(wallpaper.wallpaperComponent.getPackageName())) {
                try {
                    if (WallpaperManagerService.this.mIPackageManager.getServiceInfo(wallpaper.wallpaperComponent, 0, WallpaperManagerService.this.mCurrentUserId) == null) {
                        Slog.w(WallpaperManagerService.TAG, "Wallpaper component gone, removing: " + wallpaper.wallpaperComponent);
                        WallpaperManagerService.this.clearWallpaperLocked(false, 1, wallpaper.userId, null);
                    }
                } catch (RemoteException e) {
                    Slog.e(WallpaperManagerService.TAG, " mIPackageManager  RemoteException error");
                }
            }
            if (wallpaper.nextWallpaperComponent != null && isPackageModified(wallpaper.nextWallpaperComponent.getPackageName())) {
                try {
                    if (WallpaperManagerService.this.mIPackageManager.getServiceInfo(wallpaper.nextWallpaperComponent, 0, WallpaperManagerService.this.mCurrentUserId) == null) {
                        wallpaper.nextWallpaperComponent = null;
                    }
                } catch (RemoteException e2) {
                    Slog.e(WallpaperManagerService.TAG, " nextWallpaperComponent mIPackageManager  RemoteException error");
                }
            }
            return changed;
        }
    }

    private class ThemeSettingsObserver extends ContentObserver {
        public ThemeSettingsObserver(Handler handler) {
            super(handler);
        }

        public void startObserving(Context context) {
            context.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("theme_mode"), false, this);
        }

        public void stopObserving(Context context) {
            context.getContentResolver().unregisterContentObserver(this);
        }

        public void onChange(boolean selfChange) {
            WallpaperManagerService.this.onThemeSettingsChanged();
        }
    }

    class WallpaperConnection extends IWallpaperConnection.Stub implements ServiceConnection {
        private static final long WALLPAPER_RECONNECT_TIMEOUT_MS = 10000;
        boolean mDimensionsChanged = false;
        IWallpaperEngine mEngine;
        final WallpaperInfo mInfo;
        boolean mPaddingChanged = false;
        IRemoteCallback mReply;
        /* access modifiers changed from: private */
        public Runnable mResetRunnable = new Runnable() {
            public final void run() {
                WallpaperManagerService.WallpaperConnection.lambda$new$0(WallpaperManagerService.WallpaperConnection.this);
            }
        };
        IWallpaperService mService;
        final Binder mToken = new Binder();
        WallpaperData mWallpaper;

        /* JADX WARNING: Code restructure failed: missing block: B:14:0x004f, code lost:
            return;
         */
        public static /* synthetic */ void lambda$new$0(WallpaperConnection wallpaperConnection) {
            synchronized (WallpaperManagerService.this.mLock) {
                if (WallpaperManagerService.this.mShuttingDown) {
                    Slog.i(WallpaperManagerService.TAG, "Ignoring relaunch timeout during shutdown");
                } else if (!wallpaperConnection.mWallpaper.wallpaperUpdating && wallpaperConnection.mWallpaper.userId == WallpaperManagerService.this.mCurrentUserId) {
                    Slog.w(WallpaperManagerService.TAG, "Wallpaper reconnect timed out for " + wallpaperConnection.mWallpaper.wallpaperComponent + ", reverting to built-in wallpaper!");
                    WallpaperManagerService.this.clearWallpaperLocked(true, 1, wallpaperConnection.mWallpaper.userId, null);
                }
            }
        }

        public WallpaperConnection(WallpaperInfo info, WallpaperData wallpaper) {
            this.mInfo = info;
            this.mWallpaper = wallpaper;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            Slog.i(WallpaperManagerService.TAG, "Service Connected: " + name + " userId:" + this.mWallpaper.userId);
            synchronized (WallpaperManagerService.this.mLock) {
                if (this.mWallpaper.connection == this) {
                    this.mService = IWallpaperService.Stub.asInterface(service);
                    WallpaperManagerService.this.attachServiceLocked(this, this.mWallpaper);
                    WallpaperManagerService.this.saveSettingsLocked(this.mWallpaper.userId);
                    FgThread.getHandler().removeCallbacks(this.mResetRunnable);
                    if (name != null && !name.equals(WallpaperManagerService.this.mImageWallpaper)) {
                        WallpaperManagerService.this.mContext.sendBroadcastAsUser(new Intent("android.intent.action.WALLPAPER_CHANGED"), new UserHandle(WallpaperManagerService.this.mCurrentUserId));
                    }
                }
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            synchronized (WallpaperManagerService.this.mLock) {
                Slog.w(WallpaperManagerService.TAG, "Wallpaper service gone: " + name);
                if (!Objects.equals(name, this.mWallpaper.wallpaperComponent)) {
                    Slog.e(WallpaperManagerService.TAG, "Does not match expected wallpaper component " + this.mWallpaper.wallpaperComponent);
                }
                this.mService = null;
                this.mEngine = null;
                if (this.mWallpaper.connection == this && !this.mWallpaper.wallpaperUpdating) {
                    WallpaperManagerService.this.mContext.getMainThreadHandler().postDelayed(new Runnable() {
                        public final void run() {
                            WallpaperManagerService.WallpaperConnection.lambda$onServiceDisconnected$1(WallpaperManagerService.WallpaperConnection.this);
                        }
                    }, 1000);
                }
            }
        }

        public void scheduleTimeoutLocked() {
            Handler fgHandler = FgThread.getHandler();
            fgHandler.removeCallbacks(this.mResetRunnable);
            fgHandler.postDelayed(this.mResetRunnable, 10000);
            Slog.i(WallpaperManagerService.TAG, "Started wallpaper reconnect timeout for " + this.mWallpaper.wallpaperComponent);
        }

        /* access modifiers changed from: private */
        /* renamed from: processDisconnect */
        public void lambda$onServiceDisconnected$1(ServiceConnection connection) {
            synchronized (WallpaperManagerService.this.mLock) {
                if (connection == this.mWallpaper.connection) {
                    ComponentName wpService = this.mWallpaper.wallpaperComponent;
                    if (!this.mWallpaper.wallpaperUpdating && this.mWallpaper.userId == WallpaperManagerService.this.mCurrentUserId && !Objects.equals(WallpaperManagerService.this.mDefaultWallpaperComponent, wpService) && !Objects.equals(WallpaperManagerService.this.mImageWallpaper, wpService)) {
                        if (this.mWallpaper.lastDiedTime == 0 || this.mWallpaper.lastDiedTime + 10000 <= SystemClock.uptimeMillis()) {
                            this.mWallpaper.lastDiedTime = SystemClock.uptimeMillis();
                            WallpaperManagerService.this.clearWallpaperComponentLocked(this.mWallpaper);
                            if (WallpaperManagerService.this.bindWallpaperComponentLocked(wpService, false, false, this.mWallpaper, null)) {
                                this.mWallpaper.connection.scheduleTimeoutLocked();
                            } else {
                                Slog.w(WallpaperManagerService.TAG, "Reverting to built-in wallpaper!");
                                WallpaperManagerService.this.clearWallpaperLocked(true, 1, this.mWallpaper.userId, null);
                            }
                        } else {
                            Slog.w(WallpaperManagerService.TAG, "Reverting to built-in wallpaper!");
                            WallpaperManagerService.this.clearWallpaperLocked(true, 1, this.mWallpaper.userId, null);
                        }
                        String flattened = wpService.flattenToString();
                        EventLog.writeEvent(EventLogTags.WP_WALLPAPER_CRASHED, flattened.substring(0, Math.min(flattened.length(), 128)));
                    }
                } else {
                    Slog.i(WallpaperManagerService.TAG, "Wallpaper changed during disconnect tracking; ignoring");
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:11:0x002d, code lost:
            r0 = r1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x002e, code lost:
            if (r0 == 0) goto L_0x0037;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0030, code lost:
            com.android.server.wallpaper.WallpaperManagerService.access$100(r4.this$0, r4.mWallpaper, r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x0037, code lost:
            return;
         */
        public void onWallpaperColorsChanged(WallpaperColors primaryColors) {
            synchronized (WallpaperManagerService.this.mLock) {
                if (!WallpaperManagerService.this.mImageWallpaper.equals(this.mWallpaper.wallpaperComponent)) {
                    this.mWallpaper.primaryColors = primaryColors;
                    int which = 1;
                    if (WallpaperManagerService.this.mLockWallpaperMap.get(this.mWallpaper.userId) == null) {
                        which = 1 | 2;
                    }
                }
            }
        }

        public void attachEngine(IWallpaperEngine engine) {
            Slog.i(WallpaperManagerService.TAG, "attachEngine userId:" + this.mWallpaper.userId);
            synchronized (WallpaperManagerService.this.mLock) {
                this.mEngine = engine;
                if (this.mDimensionsChanged) {
                    try {
                        this.mEngine.setDesiredSize(this.mWallpaper.width, this.mWallpaper.height);
                    } catch (RemoteException e) {
                        Slog.w(WallpaperManagerService.TAG, "Failed to set wallpaper dimensions", e);
                    }
                    this.mDimensionsChanged = false;
                }
                if (this.mPaddingChanged) {
                    try {
                        this.mEngine.setDisplayPadding(this.mWallpaper.padding);
                    } catch (RemoteException e2) {
                        Slog.w(WallpaperManagerService.TAG, "Failed to set wallpaper padding", e2);
                    }
                    this.mPaddingChanged = false;
                }
                if (this.mInfo != null && this.mInfo.getSupportsAmbientMode()) {
                    try {
                        this.mEngine.setInAmbientMode(WallpaperManagerService.this.mInAmbientMode, false);
                    } catch (RemoteException e3) {
                        Slog.w(WallpaperManagerService.TAG, "Failed to set ambient mode state", e3);
                    }
                }
                try {
                    this.mEngine.requestWallpaperColors();
                } catch (RemoteException e4) {
                    Slog.w(WallpaperManagerService.TAG, "Failed to request wallpaper colors", e4);
                }
            }
            return;
        }

        public void engineShown(IWallpaperEngine engine) {
            synchronized (WallpaperManagerService.this.mLock) {
                if (this.mReply != null) {
                    long ident = Binder.clearCallingIdentity();
                    try {
                        this.mReply.sendResult(null);
                    } catch (RemoteException e) {
                        Binder.restoreCallingIdentity(ident);
                    }
                    this.mReply = null;
                }
            }
        }

        public ParcelFileDescriptor setWallpaper(String name) {
            synchronized (WallpaperManagerService.this.mLock) {
                if (this.mWallpaper.connection != this) {
                    return null;
                }
                ParcelFileDescriptor updateWallpaperBitmapLocked = WallpaperManagerService.this.updateWallpaperBitmapLocked(name, this.mWallpaper, null);
                return updateWallpaperBitmapLocked;
            }
        }
    }

    public static class WallpaperData {
        boolean allowBackup;
        /* access modifiers changed from: private */
        public RemoteCallbackList<IWallpaperManagerCallback> callbacks = new RemoteCallbackList<>();
        WallpaperConnection connection;
        final File cropFile;
        final Rect cropHint = new Rect(0, 0, 0, 0);
        int[] currOffsets = {-1, -1, -1, -1};
        int height = -1;
        boolean imageWallpaperPending;
        long lastDiedTime;
        String name = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        int[] nextOffsets = {-1, -1, -1, -1};
        ComponentName nextWallpaperComponent;
        final Rect padding = new Rect(0, 0, 0, 0);
        WallpaperColors primaryColors;
        IWallpaperManagerCallback setComplete;
        ThemeSettingsObserver themeSettingsObserver;
        int userId;
        ComponentName wallpaperComponent;
        final File wallpaperFile;
        int wallpaperId;
        WallpaperObserver wallpaperObserver;
        boolean wallpaperUpdating;
        int whichPending;
        int width = -1;

        WallpaperData(int userId2, String inputFileName, String cropFileName) {
            this.userId = userId2;
            File wallpaperDir = WallpaperManagerService.getWallpaperDir(userId2);
            this.wallpaperFile = new File(wallpaperDir, inputFileName);
            this.cropFile = new File(wallpaperDir, cropFileName);
        }

        /* access modifiers changed from: package-private */
        public boolean cropExists() {
            return this.cropFile.exists();
        }

        /* access modifiers changed from: package-private */
        public boolean sourceExists() {
            return this.wallpaperFile.exists();
        }

        public void setWidth(int w) {
            this.width = w;
        }

        public int getWidth() {
            return this.width;
        }

        public int getHeight() {
            return this.height;
        }

        public RemoteCallbackList<IWallpaperManagerCallback> getCallbacks() {
            return this.callbacks;
        }
    }

    private class WallpaperObserver extends FileObserver {
        final int mUserId;
        final WallpaperData mWallpaper;
        final File mWallpaperDir;
        final File mWallpaperFile = new File(this.mWallpaperDir, WallpaperManagerService.WALLPAPER);
        final File mWallpaperLockFile = new File(this.mWallpaperDir, WallpaperManagerService.WALLPAPER_LOCK_ORIG);

        public WallpaperObserver(WallpaperData wallpaper) {
            super(WallpaperManagerService.getWallpaperDir(wallpaper.userId).getAbsolutePath(), 1672);
            this.mUserId = wallpaper.userId;
            this.mWallpaperDir = WallpaperManagerService.getWallpaperDir(wallpaper.userId);
            this.mWallpaper = wallpaper;
        }

        private WallpaperData dataForEvent(boolean sysChanged, boolean lockChanged) {
            WallpaperData wallpaper = null;
            synchronized (WallpaperManagerService.this.mLock) {
                if (lockChanged) {
                    try {
                        wallpaper = WallpaperManagerService.this.mLockWallpaperMap.get(this.mUserId);
                    } catch (Throwable th) {
                        while (true) {
                            throw th;
                        }
                    }
                }
                if (wallpaper == null) {
                    wallpaper = WallpaperManagerService.this.mWallpaperMap.get(this.mUserId);
                }
            }
            return wallpaper != null ? wallpaper : this.mWallpaper;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:28:0x0071, code lost:
            if (r15.imageWallpaperPending != false) goto L_0x0079;
         */
        /* JADX WARNING: Removed duplicated region for block: B:63:0x00e1 A[SYNTHETIC, Splitter:B:63:0x00e1] */
        /* JADX WARNING: Removed duplicated region for block: B:70:0x00ee  */
        public void onEvent(int event, String path) {
            Object obj;
            WallpaperData wallpaper;
            WallpaperData wallpaper2;
            int i;
            int i2 = event;
            String str = path;
            if (str != null) {
                boolean moved = i2 == 128;
                boolean written = i2 == 8 || moved;
                File changedFile = new File(this.mWallpaperDir, str);
                boolean sysWallpaperChanged = this.mWallpaperFile.equals(changedFile);
                boolean lockWallpaperChanged = this.mWallpaperLockFile.equals(changedFile);
                int notifyColorsWhich = 0;
                WallpaperData wallpaper3 = dataForEvent(sysWallpaperChanged, lockWallpaperChanged);
                if (!moved || !lockWallpaperChanged) {
                    Object obj2 = WallpaperManagerService.this.mLock;
                    synchronized (obj2) {
                        if (sysWallpaperChanged || lockWallpaperChanged) {
                            try {
                                WallpaperManagerService.this.updateWallpaperOffsets(this.mWallpaper);
                                WallpaperManagerService.this.handleWallpaperObserverEvent(this.mWallpaper);
                                WallpaperManagerService.this.notifyCallbacksLocked(wallpaper3);
                                if (wallpaper3.wallpaperComponent != null && i2 == 8) {
                                    try {
                                    } catch (Throwable th) {
                                        th = th;
                                        obj = obj2;
                                        WallpaperData wallpaperData = wallpaper3;
                                        throw th;
                                    }
                                }
                                if (written) {
                                    SELinux.restorecon(changedFile);
                                    if (moved) {
                                        SELinux.restorecon(changedFile);
                                        WallpaperManagerService.this.loadSettingsLocked(wallpaper3.userId, true);
                                    }
                                    WallpaperManagerService.this.generateCrop(wallpaper3);
                                    wallpaper3.imageWallpaperPending = false;
                                    if (sysWallpaperChanged) {
                                        try {
                                            obj = obj2;
                                            i = 2;
                                            wallpaper2 = wallpaper3;
                                            try {
                                                WallpaperManagerService.this.bindWallpaperComponentLocked(WallpaperManagerService.this.mImageWallpaper, false, false, wallpaper2, null);
                                                notifyColorsWhich = 0 | 1;
                                            } catch (Throwable th2) {
                                                th = th2;
                                                WallpaperData wallpaperData2 = wallpaper2;
                                                throw th;
                                            }
                                        } catch (Throwable th3) {
                                            th = th3;
                                            obj = obj2;
                                            WallpaperData wallpaperData3 = wallpaper3;
                                            throw th;
                                        }
                                    } else {
                                        obj = obj2;
                                        wallpaper2 = wallpaper3;
                                        i = 2;
                                    }
                                    if (!lockWallpaperChanged) {
                                        wallpaper = wallpaper2;
                                        if ((i & wallpaper.whichPending) != 0) {
                                        }
                                        WallpaperManagerService.this.saveSettingsLocked(wallpaper.userId);
                                        if (wallpaper.setComplete != null) {
                                            try {
                                                wallpaper.setComplete.onWallpaperChanged();
                                            } catch (RemoteException e) {
                                            }
                                        }
                                    } else {
                                        wallpaper = wallpaper2;
                                    }
                                    if (!lockWallpaperChanged) {
                                        WallpaperManagerService.this.mLockWallpaperMap.remove(wallpaper.userId);
                                    }
                                    WallpaperManagerService.this.notifyLockWallpaperChanged();
                                    notifyColorsWhich |= 2;
                                    WallpaperManagerService.this.saveSettingsLocked(wallpaper.userId);
                                    if (wallpaper.setComplete != null) {
                                    }
                                } else {
                                    obj = obj2;
                                    wallpaper = wallpaper3;
                                }
                                if (notifyColorsWhich != 0) {
                                    WallpaperManagerService.this.notifyWallpaperColorsChanged(wallpaper, notifyColorsWhich);
                                }
                            } catch (Throwable th4) {
                                th = th4;
                                obj = obj2;
                                WallpaperData wallpaperData4 = wallpaper3;
                                throw th;
                            }
                        }
                        obj = obj2;
                        wallpaper = wallpaper3;
                        try {
                            if (notifyColorsWhich != 0) {
                            }
                        } catch (Throwable th5) {
                            th = th5;
                            throw th;
                        }
                    }
                } else {
                    SELinux.restorecon(changedFile);
                    WallpaperManagerService.this.notifyLockWallpaperChanged();
                    WallpaperManagerService.this.notifyWallpaperColorsChanged(wallpaper3, 2);
                }
            }
        }
    }

    private boolean needUpdateLocked(WallpaperColors colors, int themeMode) {
        boolean z = false;
        if (colors == null || themeMode == this.mThemeMode) {
            return false;
        }
        boolean result = true;
        boolean supportDarkTheme = (colors.getColorHints() & 2) != 0;
        switch (themeMode) {
            case 0:
                if (this.mThemeMode != 1) {
                    if (!supportDarkTheme) {
                        z = true;
                    }
                    result = z;
                    break;
                } else {
                    result = supportDarkTheme;
                    break;
                }
            case 1:
                if (this.mThemeMode == 0) {
                    result = supportDarkTheme;
                    break;
                }
                break;
            case 2:
                if (this.mThemeMode == 0) {
                    if (!supportDarkTheme) {
                        z = true;
                    }
                    result = z;
                    break;
                }
                break;
            default:
                Slog.w(TAG, "unkonwn theme mode " + themeMode);
                return false;
        }
        this.mThemeMode = themeMode;
        return result;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x002c, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0026, code lost:
        if (r1 == null) goto L_0x002c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0028, code lost:
        notifyWallpaperColorsChanged(r1, 1);
     */
    public void onThemeSettingsChanged() {
        synchronized (this.mLock) {
            WallpaperData wallpaper = this.mWallpaperMap.get(this.mCurrentUserId);
            if (!needUpdateLocked(wallpaper.primaryColors, Settings.Secure.getInt(this.mContext.getContentResolver(), "theme_mode", 0))) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyLockWallpaperChanged() {
        IWallpaperManagerCallback cb = this.mKeyguardListener;
        if (cb != null) {
            try {
                cb.onWallpaperChanged();
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002d, code lost:
        notifyColorListeners(r5.primaryColors, r6, r5.userId);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0034, code lost:
        if (r1 == false) goto L_0x004e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0036, code lost:
        extractColors(r5);
        r0 = r4.mLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x003b, code lost:
        monitor-enter(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x003e, code lost:
        if (r5.primaryColors != null) goto L_0x0042;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0040, code lost:
        monitor-exit(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0041, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0042, code lost:
        monitor-exit(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0043, code lost:
        notifyColorListeners(r5.primaryColors, r6, r5.userId);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x004e, code lost:
        return;
     */
    public void notifyWallpaperColorsChanged(WallpaperData wallpaper, int which) {
        synchronized (this.mLock) {
            RemoteCallbackList<IWallpaperManagerCallback> userAllColorListeners = this.mColorsChangedListeners.get(-1);
            if (!emptyCallbackList(this.mColorsChangedListeners.get(wallpaper.userId)) || !emptyCallbackList(userAllColorListeners)) {
                boolean needsExtraction = wallpaper.primaryColors == null;
            }
        }
    }

    private static <T extends IInterface> boolean emptyCallbackList(RemoteCallbackList<T> list) {
        return list == null || list.getRegisteredCallbackCount() == 0;
    }

    private void notifyColorListeners(WallpaperColors wallpaperColors, int which, int userId) {
        IWallpaperManagerCallback keyguardListener;
        int i;
        WallpaperColors wallpaperColors2;
        ArrayList<IWallpaperManagerCallback> colorListeners = new ArrayList<>();
        synchronized (this.mLock) {
            RemoteCallbackList<IWallpaperManagerCallback> currentUserColorListeners = this.mColorsChangedListeners.get(userId);
            RemoteCallbackList<IWallpaperManagerCallback> userAllColorListeners = this.mColorsChangedListeners.get(-1);
            keyguardListener = this.mKeyguardListener;
            i = 0;
            if (currentUserColorListeners != null) {
                int count = currentUserColorListeners.beginBroadcast();
                for (int i2 = 0; i2 < count; i2++) {
                    colorListeners.add(currentUserColorListeners.getBroadcastItem(i2));
                }
                currentUserColorListeners.finishBroadcast();
            }
            if (userAllColorListeners != null) {
                int count2 = userAllColorListeners.beginBroadcast();
                for (int i3 = 0; i3 < count2; i3++) {
                    colorListeners.add(userAllColorListeners.getBroadcastItem(i3));
                }
                userAllColorListeners.finishBroadcast();
            }
            wallpaperColors2 = getThemeColorsLocked(wallpaperColors);
        }
        IWallpaperManagerCallback keyguardListener2 = keyguardListener;
        int count3 = colorListeners.size();
        while (true) {
            int i4 = i;
            if (i4 >= count3) {
                break;
            }
            try {
                colorListeners.get(i4).onWallpaperColorsChanged(wallpaperColors2, which, userId);
            } catch (RemoteException e) {
            }
            i = i4 + 1;
        }
        if (keyguardListener2 != null) {
            try {
                keyguardListener2.onWallpaperColorsChanged(wallpaperColors2, which, userId);
            } catch (RemoteException e2) {
            }
        }
    }

    private void extractColors(WallpaperData wallpaper) {
        boolean imageWallpaper;
        int wallpaperId;
        String cropFile = null;
        synchronized (this.mLock) {
            if (!this.mImageWallpaper.equals(wallpaper.wallpaperComponent)) {
                if (wallpaper.wallpaperComponent != null) {
                    imageWallpaper = false;
                    if (imageWallpaper && wallpaper.cropFile != null && wallpaper.cropFile.exists()) {
                        cropFile = wallpaper.cropFile.getAbsolutePath();
                    }
                    wallpaperId = wallpaper.wallpaperId;
                }
            }
            imageWallpaper = true;
            cropFile = wallpaper.cropFile.getAbsolutePath();
            wallpaperId = wallpaper.wallpaperId;
        }
        WallpaperColors colors = null;
        if (cropFile != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(cropFile);
            if (bitmap != null) {
                colors = WallpaperColors.fromBitmap(bitmap);
                bitmap.recycle();
            }
        }
        WallpaperColors colors2 = colors;
        if (colors2 == null) {
            Slog.w(TAG, "Cannot extract colors because wallpaper could not be read.");
            return;
        }
        synchronized (this.mLock) {
            if (wallpaper.wallpaperId == wallpaperId) {
                wallpaper.primaryColors = colors2;
                saveSettingsLocked(wallpaper.userId);
            } else {
                Slog.w(TAG, "Not setting primary colors since wallpaper changed");
            }
        }
    }

    private WallpaperColors getThemeColorsLocked(WallpaperColors colors) {
        if (colors == null) {
            Slog.w(TAG, "Cannot get theme colors because WallpaperColors is null.");
            return null;
        }
        int colorHints = colors.getColorHints();
        boolean supportDarkTheme = (colorHints & 2) != 0;
        if (this.mThemeMode == 0 || ((this.mThemeMode == 1 && !supportDarkTheme) || (this.mThemeMode == 2 && supportDarkTheme))) {
            return colors;
        }
        WallpaperColors themeColors = new WallpaperColors(colors.getPrimaryColor(), colors.getSecondaryColor(), colors.getTertiaryColor());
        if (this.mThemeMode == 1) {
            colorHints &= -3;
        } else if (this.mThemeMode == 2) {
            colorHints |= 2;
        }
        themeColors.setColorHints(colorHints);
        return themeColors;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:100:0x01cb  */
    /* JADX WARNING: Removed duplicated region for block: B:103:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:97:0x01b7  */
    public void generateCrop(WallpaperData wallpaper) {
        boolean success;
        int i;
        int i2;
        WallpaperData wallpaperData = wallpaper;
        boolean success2 = false;
        Rect cropHint = new Rect(wallpaperData.cropHint);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(wallpaperData.wallpaperFile.getAbsolutePath(), options);
        if (options.outWidth <= 0) {
            Rect rect = cropHint;
        } else if (options.outHeight <= 0) {
            Rect rect2 = cropHint;
        } else {
            boolean needCrop = false;
            if (cropHint.isEmpty()) {
                cropHint.top = 0;
                cropHint.left = 0;
                cropHint.right = options.outWidth;
                cropHint.bottom = options.outHeight;
            } else {
                if (cropHint.right > options.outWidth) {
                    i = options.outWidth - cropHint.right;
                } else {
                    i = 0;
                }
                if (cropHint.bottom > options.outHeight) {
                    i2 = options.outHeight - cropHint.bottom;
                } else {
                    i2 = 0;
                }
                cropHint.offset(i, i2);
                if (cropHint.left < 0) {
                    cropHint.left = 0;
                }
                if (cropHint.top < 0) {
                    cropHint.top = 0;
                }
                needCrop = options.outHeight > cropHint.height() || options.outWidth > cropHint.width();
            }
            boolean needScale = wallpaperData.height != (cropHint.height() > cropHint.width() ? cropHint.height() : cropHint.width());
            if (needScale && wallpaperData.width == cropHint.width() && wallpaperData.height == cropHint.height()) {
                needScale = false;
                Slog.v(TAG, "same size wallpaper ,not need to scale.  w=" + cropHint.width() + " h=" + cropHint.height());
            }
            if (needCrop || needScale) {
                FileOutputStream f = null;
                BitmapFactory.Options scaler = null;
                BufferedOutputStream bos = null;
                try {
                    BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(wallpaperData.wallpaperFile.getAbsolutePath(), false);
                    int scale = 1;
                    while (2 * scale < cropHint.height() / wallpaperData.height) {
                        scale *= 2;
                    }
                    if (scale > 1) {
                        try {
                            scaler = new BitmapFactory.Options();
                            scaler.inSampleSize = scale;
                        } catch (Exception e) {
                            success = false;
                            Rect rect3 = cropHint;
                        } catch (Throwable th) {
                            th = th;
                            Rect rect4 = cropHint;
                            IoUtils.closeQuietly(bos);
                            IoUtils.closeQuietly(f);
                            throw th;
                        }
                    }
                    Bitmap cropped = decoder.decodeRegion(cropHint, scaler);
                    decoder.recycle();
                    if (cropped == null) {
                        Slog.e(TAG, "Could not decode new wallpaper");
                        Rect rect5 = cropHint;
                    } else {
                        cropHint.offsetTo(0, 0);
                        cropHint.right /= scale;
                        cropHint.bottom /= scale;
                        int destWidth = (int) (((float) cropHint.width()) * (((float) wallpaperData.height) / ((float) cropHint.height())));
                        success = false;
                        try {
                            Rect rect6 = cropHint;
                        } catch (Exception e2) {
                            Rect rect7 = cropHint;
                            IoUtils.closeQuietly(bos);
                            IoUtils.closeQuietly(f);
                            success2 = success;
                            if (!success2) {
                            }
                            if (!wallpaperData.cropFile.exists()) {
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            Rect rect8 = cropHint;
                            IoUtils.closeQuietly(bos);
                            IoUtils.closeQuietly(f);
                            throw th;
                        }
                        try {
                            Bitmap finalCrop = Bitmap.createScaledBitmap(cropped, destWidth, wallpaperData.height, true);
                            int i3 = destWidth;
                            f = new FileOutputStream(wallpaperData.cropFile);
                            bos = new BufferedOutputStream(f, 32768);
                            finalCrop.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                            bos.flush();
                            success2 = true;
                        } catch (Exception e3) {
                            IoUtils.closeQuietly(bos);
                            IoUtils.closeQuietly(f);
                            success2 = success;
                            if (!success2) {
                            }
                            if (!wallpaperData.cropFile.exists()) {
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            IoUtils.closeQuietly(bos);
                            IoUtils.closeQuietly(f);
                            throw th;
                        }
                    }
                    IoUtils.closeQuietly(bos);
                    IoUtils.closeQuietly(f);
                } catch (Exception e4) {
                    success = false;
                    Rect rect9 = cropHint;
                    IoUtils.closeQuietly(bos);
                    IoUtils.closeQuietly(f);
                    success2 = success;
                    if (!success2) {
                    }
                    if (!wallpaperData.cropFile.exists()) {
                    }
                } catch (Throwable th4) {
                    th = th4;
                    Rect rect10 = cropHint;
                    IoUtils.closeQuietly(bos);
                    IoUtils.closeQuietly(f);
                    throw th;
                }
                if (!success2) {
                    Slog.e(TAG, "Unable to apply new wallpaper");
                    wallpaperData.cropFile.delete();
                }
                if (!wallpaperData.cropFile.exists()) {
                    SELinux.restorecon(wallpaperData.cropFile.getAbsoluteFile());
                    return;
                }
                return;
            }
            success2 = FileUtils.copyFile(wallpaperData.wallpaperFile, wallpaperData.cropFile);
            if (!success2) {
                wallpaperData.cropFile.delete();
            }
            Rect rect11 = cropHint;
            if (!success2) {
            }
            if (!wallpaperData.cropFile.exists()) {
            }
        }
        Slog.w(TAG, "Invalid wallpaper data");
        success2 = false;
        if (!success2) {
        }
        if (!wallpaperData.cropFile.exists()) {
        }
    }

    /* access modifiers changed from: package-private */
    public int makeWallpaperIdLocked() {
        do {
            this.mWallpaperId++;
        } while (this.mWallpaperId == 0);
        return this.mWallpaperId;
    }

    public WallpaperManagerService(Context context) {
        this.mContext = context;
        this.mShuttingDown = false;
        this.mImageWallpaper = ComponentName.unflattenFromString(context.getResources().getString(17040204));
        this.mDefaultWallpaperComponent = WallpaperManager.getDefaultWallpaperComponent(context);
        this.mIWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
        this.mIPackageManager = AppGlobals.getPackageManager();
        this.mAppOpsManager = (AppOpsManager) this.mContext.getSystemService("appops");
        this.mMonitor = new MyPackageMonitor();
        this.mColorsChangedListeners = new SparseArray<>();
    }

    /* access modifiers changed from: package-private */
    public void initialize() {
        this.mMonitor.register(this.mContext, null, UserHandle.ALL, true);
        getWallpaperDir(0).mkdirs();
        loadSettingsLocked(0, false);
        getWallpaperSafeLocked(0, 1);
    }

    /* access modifiers changed from: private */
    public static File getWallpaperDir(int userId) {
        return Environment.getUserSystemDirectory(userId);
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        super.finalize();
        for (int i = 0; i < this.mWallpaperMap.size(); i++) {
            this.mWallpaperMap.valueAt(i).wallpaperObserver.stopWatching();
        }
    }

    /* access modifiers changed from: package-private */
    public void systemReady() {
        initialize();
        WallpaperData wallpaper = this.mWallpaperMap.get(0);
        if (this.mImageWallpaper.equals(wallpaper.nextWallpaperComponent)) {
            if (!wallpaper.cropExists()) {
                generateCrop(wallpaper);
            }
            if (!wallpaper.cropExists()) {
                clearWallpaperLocked(false, 1, 0, null);
            }
        }
        IntentFilter userFilter = new IntentFilter();
        userFilter.addAction("android.intent.action.USER_REMOVED");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.USER_REMOVED".equals(intent.getAction())) {
                    WallpaperManagerService.this.onRemoveUser(intent.getIntExtra("android.intent.extra.user_handle", -10000));
                }
            }
        }, userFilter);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.ACTION_SHUTDOWN".equals(intent.getAction())) {
                    synchronized (WallpaperManagerService.this.mLock) {
                        WallpaperManagerService.this.mShuttingDown = true;
                    }
                }
            }
        }, new IntentFilter("android.intent.action.ACTION_SHUTDOWN"));
        try {
            ActivityManager.getService().registerUserSwitchObserver(new UserSwitchObserver() {
                public void onUserSwitching(int newUserId, IRemoteCallback reply) {
                    WallpaperManagerService.this.switchUser(newUserId, reply);
                }
            }, TAG);
        } catch (RemoteException e) {
            e.rethrowAsRuntimeException();
        }
    }

    public String getName() {
        String str;
        if (Binder.getCallingUid() == 1000) {
            synchronized (this.mLock) {
                str = this.mWallpaperMap.get(0).name;
            }
            return str;
        }
        throw new RuntimeException("getName() can only be called from the system process");
    }

    /* access modifiers changed from: package-private */
    public void stopObserver(WallpaperData wallpaper) {
        if (wallpaper != null) {
            if (wallpaper.wallpaperObserver != null) {
                wallpaper.wallpaperObserver.stopWatching();
                wallpaper.wallpaperObserver = null;
            }
            if (wallpaper.themeSettingsObserver != null) {
                wallpaper.themeSettingsObserver.stopObserving(this.mContext);
                wallpaper.themeSettingsObserver = null;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void stopObserversLocked(int userId) {
        stopObserver(this.mWallpaperMap.get(userId));
        stopObserver(this.mLockWallpaperMap.get(userId));
        this.mWallpaperMap.remove(userId);
        this.mLockWallpaperMap.remove(userId);
    }

    public void onBootPhase(int phase) {
        if (phase == 550) {
            systemReady();
        } else if (phase == 600) {
            switchUser(0, null);
        }
    }

    public void onUnlockUser(final int userId) {
        synchronized (this.mLock) {
            if (this.mCurrentUserId == userId) {
                if (this.mWaitingForUnlock) {
                    switchWallpaper(getWallpaperSafeLocked(userId, 1), null);
                }
                if (this.mUserRestorecon.get(userId) != Boolean.TRUE) {
                    this.mUserRestorecon.put(userId, Boolean.TRUE);
                    BackgroundThread.getHandler().post(new Runnable() {
                        public void run() {
                            File wallpaperDir = WallpaperManagerService.getWallpaperDir(userId);
                            for (String filename : WallpaperManagerService.sPerUserFiles) {
                                File f = new File(wallpaperDir, filename);
                                if (f.exists()) {
                                    SELinux.restorecon(f);
                                }
                            }
                        }
                    });
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onRemoveUser(int userId) {
        if (userId >= 1) {
            File wallpaperDir = getWallpaperDir(userId);
            synchronized (this.mLock) {
                stopObserversLocked(userId);
                for (String filename : sPerUserFiles) {
                    new File(wallpaperDir, filename).delete();
                }
                this.mUserRestorecon.remove(userId);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void switchUser(int userId, IRemoteCallback reply) {
        synchronized (this.mLock) {
            if (this.mCurrentUserId != userId || !this.mIsLoadLiveWallpaper) {
                this.mIsLoadLiveWallpaper = true;
                this.mCurrentUserId = userId;
                if (this.mLastWallpaper != null) {
                    handleWallpaperObserverEvent(this.mLastWallpaper);
                }
                WallpaperData systemWallpaper = getWallpaperSafeLocked(userId, 1);
                WallpaperData tmpLockWallpaper = this.mLockWallpaperMap.get(userId);
                WallpaperData lockWallpaper = tmpLockWallpaper == null ? systemWallpaper : tmpLockWallpaper;
                if (systemWallpaper.wallpaperObserver == null) {
                    systemWallpaper.wallpaperObserver = new WallpaperObserver(systemWallpaper);
                    systemWallpaper.wallpaperObserver.startWatching();
                }
                if (systemWallpaper.themeSettingsObserver == null) {
                    systemWallpaper.themeSettingsObserver = new ThemeSettingsObserver(null);
                    systemWallpaper.themeSettingsObserver.startObserving(this.mContext);
                }
                this.mThemeMode = Settings.Secure.getInt(this.mContext.getContentResolver(), "theme_mode", 0);
                switchWallpaper(systemWallpaper, reply);
                FgThread.getHandler().post(new Runnable(systemWallpaper, lockWallpaper) {
                    private final /* synthetic */ WallpaperManagerService.WallpaperData f$1;
                    private final /* synthetic */ WallpaperManagerService.WallpaperData f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void run() {
                        WallpaperManagerService.lambda$switchUser$0(WallpaperManagerService.this, this.f$1, this.f$2);
                    }
                });
            }
        }
    }

    public static /* synthetic */ void lambda$switchUser$0(WallpaperManagerService wallpaperManagerService, WallpaperData systemWallpaper, WallpaperData lockWallpaper) {
        wallpaperManagerService.notifyWallpaperColorsChanged(systemWallpaper, 1);
        wallpaperManagerService.notifyWallpaperColorsChanged(lockWallpaper, 2);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x006f, code lost:
        return;
     */
    public void switchWallpaper(WallpaperData wallpaper, IRemoteCallback reply) {
        ServiceInfo si;
        WallpaperData wallpaperData = wallpaper;
        synchronized (this.mLock) {
            try {
                this.mWaitingForUnlock = false;
                ComponentName cname = wallpaperData.wallpaperComponent != null ? wallpaperData.wallpaperComponent : wallpaperData.nextWallpaperComponent;
                if (!bindWallpaperComponentLocked(cname, true, false, wallpaperData, reply)) {
                    try {
                        si = this.mIPackageManager.getServiceInfo(cname, 262144, wallpaperData.userId);
                    } catch (RemoteException e) {
                        si = null;
                    }
                    if (si == null) {
                        Slog.w(TAG, "Failure starting previous wallpaper; clearing");
                        clearWallpaperLocked(false, 1, wallpaperData.userId, reply);
                    } else {
                        Slog.w(TAG, "Wallpaper isn't direct boot aware; using fallback until unlocked");
                        wallpaperData.wallpaperComponent = wallpaperData.nextWallpaperComponent;
                        WallpaperData fallback = new WallpaperData(wallpaperData.userId, WALLPAPER_LOCK_ORIG, WALLPAPER_LOCK_CROP);
                        ensureSaneWallpaperData(fallback);
                        bindWallpaperComponentLocked(this.mImageWallpaper, true, false, fallback, reply);
                        this.mWaitingForUnlock = true;
                    }
                } else {
                    IRemoteCallback iRemoteCallback = reply;
                }
            } catch (Throwable th) {
                th = th;
                throw th;
            }
        }
    }

    public void clearWallpaper(String callingPackage, int which, int userId) {
        checkPermission("android.permission.SET_WALLPAPER");
        if (isWallpaperSupported(callingPackage) && isSetWallpaperAllowed(callingPackage)) {
            int userId2 = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, true, "clearWallpaper", null);
            WallpaperData data = null;
            synchronized (this.mLock) {
                clearWallpaperLocked(false, which, userId2, null);
                if (which == 2) {
                    data = this.mLockWallpaperMap.get(userId2);
                }
                if (which == 1 || data == null) {
                    data = this.mWallpaperMap.get(userId2);
                }
            }
            if (data != null) {
                notifyWallpaperColorsChanged(data, which);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void clearWallpaperLocked(boolean defaultFailed, int which, int userId, IRemoteCallback reply) {
        WallpaperData wallpaper;
        ComponentName componentName;
        int i = which;
        int i2 = userId;
        IRemoteCallback iRemoteCallback = reply;
        if (i == 1 || i == 2) {
            if (i == 2) {
                wallpaper = this.mLockWallpaperMap.get(i2);
                if (wallpaper == null) {
                    return;
                }
            } else {
                wallpaper = this.mWallpaperMap.get(i2);
                if (wallpaper == null) {
                    loadSettingsLocked(i2, false);
                    wallpaper = this.mWallpaperMap.get(i2);
                }
            }
            WallpaperData wallpaper2 = wallpaper;
            if (wallpaper2 != null) {
                long ident = Binder.clearCallingIdentity();
                try {
                    if (wallpaper2.wallpaperFile.exists()) {
                        wallpaper2.wallpaperFile.delete();
                        wallpaper2.cropFile.delete();
                        if (i == 2) {
                            this.mLockWallpaperMap.remove(i2);
                            IWallpaperManagerCallback cb = this.mKeyguardListener;
                            if (cb != null) {
                                try {
                                    cb.onWallpaperChanged();
                                } catch (RemoteException e) {
                                }
                            }
                            saveSettingsLocked(i2);
                            return;
                        }
                    }
                    RuntimeException e2 = null;
                    try {
                        wallpaper2.primaryColors = null;
                        wallpaper2.imageWallpaperPending = false;
                        if (i2 != this.mCurrentUserId) {
                            Binder.restoreCallingIdentity(ident);
                            return;
                        }
                        this.mDefaultWallpaperComponent = null;
                        if (defaultFailed) {
                            componentName = this.mImageWallpaper;
                        } else {
                            componentName = null;
                        }
                        if (bindWallpaperComponentLocked(componentName, true, false, wallpaper2, iRemoteCallback)) {
                            Binder.restoreCallingIdentity(ident);
                            return;
                        }
                        Slog.e(TAG, "Default wallpaper component not found!", e2);
                        clearWallpaperComponentLocked(wallpaper2);
                        if (iRemoteCallback != null) {
                            try {
                                iRemoteCallback.sendResult(null);
                            } catch (RemoteException e3) {
                            }
                        }
                        Binder.restoreCallingIdentity(ident);
                    } catch (IllegalArgumentException e1) {
                        e2 = e1;
                    }
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        } else {
            throw new IllegalArgumentException("Must specify exactly one kind of wallpaper to clear");
        }
    }

    /* JADX INFO: finally extract failed */
    public boolean hasNamedWallpaper(String name) {
        synchronized (this.mLock) {
            long ident = Binder.clearCallingIdentity();
            try {
                List<UserInfo> users = ((UserManager) this.mContext.getSystemService("user")).getUsers();
                Binder.restoreCallingIdentity(ident);
                for (UserInfo user : users) {
                    if (!user.isManagedProfile()) {
                        WallpaperData wd = this.mWallpaperMap.get(user.id);
                        if (wd == null) {
                            loadSettingsLocked(user.id, false);
                            wd = this.mWallpaperMap.get(user.id);
                        }
                        if (wd != null && name.equals(wd.name)) {
                            return true;
                        }
                    }
                }
                return false;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        }
    }

    private Point getDefaultDisplaySize() {
        Point p = new Point();
        ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay().getRealSize(p);
        return p;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0065, code lost:
        return;
     */
    public void setDimensionHints(int width, int height, String callingPackage) throws RemoteException {
        checkPermission("android.permission.SET_WALLPAPER_HINTS");
        if (isWallpaperSupported(callingPackage)) {
            synchronized (this.mLock) {
                int userId = UserHandle.getCallingUserId();
                WallpaperData wallpaper = getWallpaperSafeLocked(userId, 1);
                if (width <= 0 || height <= 0) {
                    throw new IllegalArgumentException("width and height must be > 0");
                }
                Point displaySize = getDefaultDisplaySize();
                int width2 = Math.max(width, displaySize.x);
                int height2 = Math.max(height, displaySize.y);
                if (!(width2 == wallpaper.width && height2 == wallpaper.height)) {
                    wallpaper.width = width2;
                    wallpaper.height = height2;
                    saveSettingsLocked(userId);
                    if (this.mCurrentUserId == userId) {
                        if (wallpaper.connection != null) {
                            if (wallpaper.connection.mEngine != null) {
                                try {
                                    wallpaper.connection.mEngine.setDesiredSize(width2, height2);
                                } catch (RemoteException e) {
                                }
                                notifyCallbacksLocked(wallpaper);
                            } else if (wallpaper.connection.mService != null) {
                                wallpaper.connection.mDimensionsChanged = true;
                            }
                        }
                    }
                }
            }
        }
    }

    public int getWidthHint() throws RemoteException {
        synchronized (this.mLock) {
            WallpaperData wallpaper = this.mWallpaperMap.get(UserHandle.getCallingUserId());
            if (wallpaper == null) {
                return 0;
            }
            int i = wallpaper.width;
            return i;
        }
    }

    public int getHeightHint() throws RemoteException {
        synchronized (this.mLock) {
            WallpaperData wallpaper = this.mWallpaperMap.get(UserHandle.getCallingUserId());
            if (wallpaper == null) {
                return 0;
            }
            int i = wallpaper.height;
            return i;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0060, code lost:
        return;
     */
    public void setDisplayPadding(Rect padding, String callingPackage) {
        checkPermission("android.permission.SET_WALLPAPER_HINTS");
        if (isWallpaperSupported(callingPackage)) {
            synchronized (this.mLock) {
                int userId = UserHandle.getCallingUserId();
                WallpaperData wallpaper = getWallpaperSafeLocked(userId, 1);
                if (padding.left < 0 || padding.top < 0 || padding.right < 0 || padding.bottom < 0) {
                    throw new IllegalArgumentException("padding must be positive: " + padding);
                } else if (!padding.equals(wallpaper.padding)) {
                    wallpaper.padding.set(padding);
                    saveSettingsLocked(userId);
                    if (this.mCurrentUserId == userId) {
                        if (wallpaper.connection != null) {
                            if (wallpaper.connection.mEngine != null) {
                                try {
                                    wallpaper.connection.mEngine.setDisplayPadding(padding);
                                } catch (RemoteException e) {
                                }
                                notifyCallbacksLocked(wallpaper);
                            } else if (wallpaper.connection.mService != null) {
                                wallpaper.connection.mPaddingChanged = true;
                            }
                        }
                    }
                }
            }
        }
    }

    private void enforceCallingOrSelfPermissionAndAppOp(String permission, String callingPkg, int callingUid, String message) {
        this.mContext.enforceCallingOrSelfPermission(permission, message);
        String opName = AppOpsManager.permissionToOp(permission);
        if (opName != null && this.mAppOpsManager.noteOp(opName, callingUid, callingPkg) != 0) {
            throw new SecurityException(message + ": " + callingPkg + " is not allowed to " + permission);
        }
    }

    public ParcelFileDescriptor getWallpaper(String callingPkg, IWallpaperManagerCallback cb, int which, Bundle outParams, int wallpaperUserId) {
        SparseArray<WallpaperData> whichSet;
        IWallpaperManagerCallback iWallpaperManagerCallback = cb;
        int i = which;
        Bundle bundle = outParams;
        if (this.mContext.checkCallingOrSelfPermission("android.permission.READ_WALLPAPER_INTERNAL") != 0) {
            enforceCallingOrSelfPermissionAndAppOp("android.permission.READ_EXTERNAL_STORAGE", callingPkg, Binder.getCallingUid(), "read wallpaper");
        } else {
            String str = callingPkg;
        }
        int wallpaperUserId2 = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), wallpaperUserId, false, true, "getWallpaper", null);
        if (i == 1 || i == 2) {
            synchronized (this.mLock) {
                if (i == 2) {
                    try {
                        whichSet = this.mLockWallpaperMap;
                    } catch (FileNotFoundException e) {
                        Slog.w(TAG, "Error getting wallpaper", e);
                        return null;
                    } catch (NullPointerException e2) {
                        Slog.w(TAG, "getting wallpaper null", e2);
                        return null;
                    } catch (Throwable th) {
                        throw th;
                    }
                } else {
                    whichSet = this.mWallpaperMap;
                }
                WallpaperData wallpaper = whichSet.get(wallpaperUserId2);
                if (wallpaper == null) {
                    return null;
                }
                if (bundle != null) {
                    bundle.putInt("width", wallpaper.width);
                    bundle.putInt("height", wallpaper.height);
                }
                if (iWallpaperManagerCallback != null && !wallpaper.callbacks.isContainIBinder(iWallpaperManagerCallback)) {
                    wallpaper.callbacks.register(iWallpaperManagerCallback);
                }
                if (!wallpaper.cropFile.exists()) {
                    return null;
                }
                ParcelFileDescriptor open = ParcelFileDescriptor.open(wallpaper.cropFile, 268435456);
                return open;
            }
        }
        throw new IllegalArgumentException("Must specify exactly one kind of wallpaper to read");
    }

    public WallpaperInfo getWallpaperInfo(int userId) {
        int userId2 = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, true, "getWallpaperInfo", null);
        synchronized (this.mLock) {
            WallpaperData wallpaper = this.mWallpaperMap.get(userId2);
            if (wallpaper == null || wallpaper.connection == null) {
                return null;
            }
            WallpaperInfo wallpaperInfo = wallpaper.connection.mInfo;
            return wallpaperInfo;
        }
    }

    public int getWallpaperIdForUser(int which, int userId) {
        int userId2 = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, true, "getWallpaperIdForUser", null);
        if (which == 1 || which == 2) {
            SparseArray<WallpaperData> map = which == 2 ? this.mLockWallpaperMap : this.mWallpaperMap;
            synchronized (this.mLock) {
                WallpaperData wallpaper = map.get(userId2);
                if (wallpaper == null) {
                    return -1;
                }
                int i = wallpaper.wallpaperId;
                return i;
            }
        }
        throw new IllegalArgumentException("Must specify exactly one kind of wallpaper");
    }

    public void registerWallpaperColorsCallback(IWallpaperManagerCallback cb, int userId) {
        int userId2 = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, true, true, "registerWallpaperColorsCallback", null);
        synchronized (this.mLock) {
            RemoteCallbackList<IWallpaperManagerCallback> userColorsChangedListeners = this.mColorsChangedListeners.get(userId2);
            if (userColorsChangedListeners == null) {
                userColorsChangedListeners = new RemoteCallbackList<>();
                this.mColorsChangedListeners.put(userId2, userColorsChangedListeners);
            }
            userColorsChangedListeners.register(cb);
        }
    }

    public void unregisterWallpaperColorsCallback(IWallpaperManagerCallback cb, int userId) {
        int userId2 = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, true, true, "unregisterWallpaperColorsCallback", null);
        synchronized (this.mLock) {
            RemoteCallbackList<IWallpaperManagerCallback> userColorsChangedListeners = this.mColorsChangedListeners.get(userId2);
            if (userColorsChangedListeners != null) {
                userColorsChangedListeners.unregister(cb);
            }
        }
    }

    public void setInAmbientMode(boolean inAmbienMode, boolean animated) {
        IWallpaperEngine engine;
        IWallpaperEngine engine2;
        synchronized (this.mLock) {
            this.mInAmbientMode = inAmbienMode;
            WallpaperData data = this.mWallpaperMap.get(this.mCurrentUserId);
            if (data == null || data.connection == null || data.connection.mInfo == null || !data.connection.mInfo.getSupportsAmbientMode()) {
                engine = null;
            } else {
                engine = data.connection.mEngine;
            }
            engine2 = engine;
        }
        if (engine2 != null) {
            try {
                engine2.setInAmbientMode(inAmbienMode, animated);
            } catch (RemoteException e) {
            }
        }
    }

    public boolean setLockWallpaperCallback(IWallpaperManagerCallback cb) {
        checkPermission("android.permission.INTERNAL_SYSTEM_WINDOW");
        synchronized (this.mLock) {
            this.mKeyguardListener = cb;
        }
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x004b, code lost:
        if (r0 == false) goto L_0x0050;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x004d, code lost:
        extractColors(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0050, code lost:
        r1 = r9.mLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0052, code lost:
        monitor-enter(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
        r3 = getThemeColorsLocked(r2.primaryColors);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0059, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x005a, code lost:
        return r3;
     */
    public WallpaperColors getWallpaperColors(int which, int userId) throws RemoteException {
        boolean shouldExtract = true;
        if (which == 2 || which == 1) {
            int userId2 = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, true, "getWallpaperColors", null);
            WallpaperData wallpaperData = null;
            synchronized (this.mLock) {
                if (which == 2) {
                    try {
                        wallpaperData = this.mLockWallpaperMap.get(userId2);
                    } catch (Throwable th) {
                        while (true) {
                            throw th;
                        }
                    }
                }
                if (wallpaperData == null) {
                    wallpaperData = this.mWallpaperMap.get(userId2);
                }
                if (wallpaperData == null) {
                    return null;
                }
                if (wallpaperData.primaryColors != null) {
                    shouldExtract = false;
                }
            }
        } else {
            throw new IllegalArgumentException("which should be either FLAG_LOCK or FLAG_SYSTEM");
        }
    }

    public ParcelFileDescriptor setWallpaper(String name, String callingPackage, Rect cropHint, boolean allowBackup, Bundle extras, int which, IWallpaperManagerCallback completion, int userId) {
        Rect cropHint2;
        String str = callingPackage;
        Rect rect = cropHint;
        int i = which;
        HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.WALLPAPERMANAGER_SETWALLPAPER);
        if (isMdmDisableChangeWallpaper()) {
            return null;
        }
        int userId2 = ActivityManager.handleIncomingUser(getCallingPid(), getCallingUid(), userId, false, true, "changing wallpaper", null);
        checkPermission("android.permission.SET_WALLPAPER");
        if ((i & 3) == 0) {
            String str2 = name;
            boolean z = allowBackup;
            Bundle bundle = extras;
            IWallpaperManagerCallback iWallpaperManagerCallback = completion;
            Slog.e(TAG, "Must specify a valid wallpaper category to set");
            throw new IllegalArgumentException("Must specify a valid wallpaper category to set");
        } else if (!isWallpaperSupported(str) || !isSetWallpaperAllowed(str)) {
            String str3 = name;
            boolean z2 = allowBackup;
            Bundle bundle2 = extras;
            IWallpaperManagerCallback iWallpaperManagerCallback2 = completion;
            return null;
        } else {
            if (rect == null) {
                cropHint2 = new Rect(0, 0, 0, 0);
                Rect rect2 = cropHint2;
            } else if (cropHint.isEmpty() || rect.left < 0 || rect.top < 0) {
                String str4 = name;
                boolean z3 = allowBackup;
                Bundle bundle3 = extras;
                IWallpaperManagerCallback iWallpaperManagerCallback3 = completion;
                throw new IllegalArgumentException("Invalid crop rect supplied: " + rect);
            } else {
                cropHint2 = rect;
            }
            synchronized (this.mLock) {
                if ((i & 3) == 0) {
                    try {
                        if (this.mLockWallpaperMap.get(userId2) == null) {
                            migrateSystemToLockWallpaperLocked(userId2);
                        }
                    } catch (Throwable th) {
                        th = th;
                        String str5 = name;
                        boolean z4 = allowBackup;
                        Bundle bundle4 = extras;
                        IWallpaperManagerCallback iWallpaperManagerCallback4 = completion;
                        throw th;
                    }
                }
                WallpaperData wallpaper = getWallpaperSafeLocked(userId2, i);
                long ident = Binder.clearCallingIdentity();
                try {
                    ParcelFileDescriptor pfd = updateWallpaperBitmapLocked(name, wallpaper, extras);
                    if (pfd != null) {
                        wallpaper.imageWallpaperPending = true;
                        wallpaper.whichPending = i;
                        try {
                            wallpaper.setComplete = completion;
                            wallpaper.cropHint.set(cropHint2);
                            try {
                                wallpaper.allowBackup = allowBackup;
                            } catch (Throwable th2) {
                                th = th2;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            boolean z5 = allowBackup;
                            Binder.restoreCallingIdentity(ident);
                            throw th;
                        }
                    } else {
                        boolean z6 = allowBackup;
                        IWallpaperManagerCallback iWallpaperManagerCallback5 = completion;
                    }
                    Binder.restoreCallingIdentity(ident);
                    return pfd;
                } catch (Throwable th4) {
                    th = th4;
                    throw th;
                }
            }
        }
    }

    private void migrateSystemToLockWallpaperLocked(int userId) {
        WallpaperData sysWP = this.mWallpaperMap.get(userId);
        if (sysWP != null) {
            WallpaperData lockWP = new WallpaperData(userId, WALLPAPER_LOCK_ORIG, WALLPAPER_LOCK_CROP);
            lockWP.wallpaperId = sysWP.wallpaperId;
            lockWP.cropHint.set(sysWP.cropHint);
            lockWP.width = sysWP.width;
            lockWP.height = sysWP.height;
            lockWP.allowBackup = sysWP.allowBackup;
            lockWP.primaryColors = sysWP.primaryColors;
            try {
                Os.rename(sysWP.wallpaperFile.getAbsolutePath(), lockWP.wallpaperFile.getAbsolutePath());
                Os.rename(sysWP.cropFile.getAbsolutePath(), lockWP.cropFile.getAbsolutePath());
                this.mLockWallpaperMap.put(userId, lockWP);
            } catch (ErrnoException e) {
                Slog.e(TAG, "Can't migrate system wallpaper: " + e.getMessage());
                lockWP.wallpaperFile.delete();
                lockWP.cropFile.delete();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public ParcelFileDescriptor updateWallpaperBitmapLocked(String name, WallpaperData wallpaper, Bundle extras) {
        if (name == null) {
            name = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        }
        try {
            File dir = getWallpaperDir(wallpaper.userId);
            if (!dir.exists()) {
                dir.mkdir();
                FileUtils.setPermissions(dir.getPath(), 505, -1, -1);
            }
            ParcelFileDescriptor fd = ParcelFileDescriptor.open(wallpaper.wallpaperFile, 1006632960);
            if (!SELinux.restorecon(wallpaper.wallpaperFile)) {
                return null;
            }
            wallpaper.name = name;
            wallpaper.wallpaperId = makeWallpaperIdLocked();
            if (extras != null) {
                extras.putInt("android.service.wallpaper.extra.ID", wallpaper.wallpaperId);
            }
            wallpaper.primaryColors = null;
            return fd;
        } catch (FileNotFoundException e) {
            Slog.w(TAG, "Error setting wallpaper", e);
            return null;
        }
    }

    public void setWallpaperComponentChecked(ComponentName name, String callingPackage, int userId) {
        if (isWallpaperSupported(callingPackage) && isSetWallpaperAllowed(callingPackage)) {
            setWallpaperComponent(name, userId);
        }
    }

    public void setWallpaperComponent(ComponentName name) {
        setWallpaperComponent(name, UserHandle.getCallingUserId());
    }

    private void setWallpaperComponent(ComponentName name, int userId) {
        WallpaperData wallpaper;
        if (!isMdmDisableChangeWallpaper()) {
            int userId2 = ActivityManager.handleIncomingUser(getCallingPid(), getCallingUid(), userId, false, true, "changing live wallpaper", null);
            checkPermission("android.permission.SET_WALLPAPER_COMPONENT");
            int which = 1;
            boolean shouldNotifyColors = false;
            synchronized (this.mLock) {
                wallpaper = this.mWallpaperMap.get(userId2);
                if (wallpaper != null) {
                    long ident = Binder.clearCallingIdentity();
                    if (this.mLockWallpaperMap.get(userId2) == null) {
                        which = 1 | 2;
                    }
                    try {
                        wallpaper.imageWallpaperPending = false;
                        boolean same = changingToSame(name, wallpaper);
                        handleWallpaperObserverEvent(wallpaper);
                        if (bindWallpaperComponentLocked(name, false, true, wallpaper, null)) {
                            if (!same) {
                                wallpaper.primaryColors = null;
                            }
                            wallpaper.nextWallpaperComponent = wallpaper.wallpaperComponent;
                            wallpaper.wallpaperId = makeWallpaperIdLocked();
                            notifyCallbacksLocked(wallpaper);
                            shouldNotifyColors = true;
                        }
                    } finally {
                        Binder.restoreCallingIdentity(ident);
                    }
                } else {
                    throw new IllegalStateException("Wallpaper not yet initialized for user " + userId2);
                }
            }
            WallpaperData wallpaper2 = wallpaper;
            if (shouldNotifyColors) {
                notifyWallpaperColorsChanged(wallpaper2, which);
            }
        }
    }

    private boolean changingToSame(ComponentName componentName, WallpaperData wallpaper) {
        if (wallpaper.connection != null) {
            if (wallpaper.wallpaperComponent == null) {
                if (componentName == null) {
                    return true;
                }
            } else if (wallpaper.wallpaperComponent.equals(componentName)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00f0, code lost:
        r9 = new android.app.WallpaperInfo(r1.mContext, (android.content.pm.ResolveInfo) r11.get(r12));
     */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x01f0  */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x01f6  */
    public boolean bindWallpaperComponentLocked(ComponentName componentName, boolean force, boolean fromUser, WallpaperData wallpaper, IRemoteCallback reply) {
        ComponentName componentName2 = componentName;
        WallpaperData wallpaperData = wallpaper;
        Slog.v(TAG, "bindWallpaperComponentLocked: componentName=" + componentName2);
        if (!force && changingToSame(componentName2, wallpaperData)) {
            return true;
        }
        if (componentName2 == null) {
            try {
                componentName2 = this.mDefaultWallpaperComponent;
                if (componentName2 == null) {
                    componentName2 = this.mImageWallpaper;
                    Slog.v(TAG, "No default component; using image wallpaper");
                }
            } catch (XmlPullParserException e) {
                if (!fromUser) {
                    Slog.w(TAG, e);
                    return false;
                }
                throw new IllegalArgumentException(e);
            } catch (IOException e2) {
                if (!fromUser) {
                    Slog.w(TAG, e2);
                    return false;
                }
                throw new IllegalArgumentException(e2);
            } catch (RemoteException e3) {
                e = e3;
                IRemoteCallback iRemoteCallback = reply;
                String msg = "Remote exception for " + componentName2 + "\n" + e;
                if (fromUser) {
                }
            }
        }
        int serviceUserId = wallpaperData.userId;
        ServiceInfo si = this.mIPackageManager.getServiceInfo(componentName2, 4224, serviceUserId);
        if (si == null) {
            Slog.w(TAG, "Attempted wallpaper " + componentName2 + " is unavailable");
            this.mIsLoadLiveWallpaper = false;
            return false;
        } else if (!"android.permission.BIND_WALLPAPER".equals(si.permission)) {
            String msg2 = "Selected service does not require android.permission.BIND_WALLPAPER: " + componentName2;
            if (!fromUser) {
                Slog.w(TAG, msg2);
                return false;
            }
            throw new SecurityException(msg2);
        } else {
            WallpaperInfo wi = null;
            Intent intent = new Intent("android.service.wallpaper.WallpaperService");
            if (componentName2 != null && !componentName2.equals(this.mImageWallpaper)) {
                List list = this.mIPackageManager.queryIntentServices(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), 128, serviceUserId).getList();
                int i = 0;
                while (true) {
                    int i2 = i;
                    if (i2 >= list.size()) {
                        break;
                    }
                    ServiceInfo rsi = ((ResolveInfo) list.get(i2)).serviceInfo;
                    if (rsi.name.equals(si.name) && rsi.packageName.equals(si.packageName)) {
                        break;
                    }
                    i = i2 + 1;
                }
                if (wi == null) {
                    String msg3 = "Selected service is not a wallpaper: " + componentName2;
                    if (!fromUser) {
                        Slog.w(TAG, msg3);
                        return false;
                    }
                    throw new SecurityException(msg3);
                }
            }
            WallpaperConnection newConn = new WallpaperConnection(wi, wallpaperData);
            intent.setComponent(componentName2);
            intent.putExtra("android.intent.extra.client_label", 17041351);
            intent.putExtra("android.intent.extra.client_intent", PendingIntent.getActivityAsUser(this.mContext, 0, Intent.createChooser(new Intent("android.intent.action.SET_WALLPAPER"), this.mContext.getText(17039750)), 0, null, new UserHandle(serviceUserId)));
            if (!this.mContext.bindServiceAsUser(intent, newConn, 570425345, new UserHandle(serviceUserId))) {
                String msg4 = "Unable to bind service: " + componentName2;
                if (!fromUser) {
                    Slog.w(TAG, msg4);
                    return false;
                }
                throw new IllegalArgumentException(msg4);
            }
            reportWallpaper(componentName2);
            if (wallpaperData.userId == this.mCurrentUserId && this.mLastWallpaper != null) {
                detachWallpaperLocked(this.mLastWallpaper);
            }
            wallpaperData.wallpaperComponent = componentName2;
            wallpaperData.connection = newConn;
            try {
                newConn.mReply = reply;
                try {
                    if (wallpaperData.userId == this.mCurrentUserId) {
                        this.mIWindowManager.addWindowToken(newConn.mToken, 2013, 0);
                        this.mLastWallpaper = wallpaperData;
                    }
                } catch (RemoteException e4) {
                }
                return true;
            } catch (RemoteException e5) {
                e = e5;
                String msg5 = "Remote exception for " + componentName2 + "\n" + e;
                if (fromUser) {
                    Slog.w(TAG, msg5);
                    return false;
                }
                throw new IllegalArgumentException(msg5);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void detachWallpaperLocked(WallpaperData wallpaper) {
        if (wallpaper.connection != null) {
            if (wallpaper.connection.mReply != null) {
                try {
                    wallpaper.connection.mReply.sendResult(null);
                } catch (RemoteException e) {
                }
                wallpaper.connection.mReply = null;
            }
            if (wallpaper.connection.mEngine != null) {
                try {
                    wallpaper.connection.mEngine.destroy();
                } catch (RemoteException e2) {
                }
            }
            this.mContext.unbindService(wallpaper.connection);
            try {
                this.mIWindowManager.removeWindowToken(wallpaper.connection.mToken, 0);
            } catch (RemoteException e3) {
            }
            wallpaper.connection.mService = null;
            wallpaper.connection.mEngine = null;
            wallpaper.connection = null;
        }
    }

    /* access modifiers changed from: package-private */
    public void clearWallpaperComponentLocked(WallpaperData wallpaper) {
        wallpaper.wallpaperComponent = null;
        detachWallpaperLocked(wallpaper);
    }

    /* access modifiers changed from: package-private */
    public void attachServiceLocked(WallpaperConnection conn, WallpaperData wallpaper) {
        try {
            conn.mService.attach(conn, conn.mToken, 2013, false, wallpaper.width, wallpaper.height, wallpaper.padding);
        } catch (RemoteException e) {
            Slog.w(TAG, "Failed attaching wallpaper; clearing", e);
            if (!wallpaper.wallpaperUpdating) {
                bindWallpaperComponentLocked(null, false, false, wallpaper, null);
            }
        }
    }

    /* access modifiers changed from: private */
    public void notifyCallbacksLocked(WallpaperData wallpaper) {
        synchronized (wallpaper.callbacks) {
            int n = wallpaper.callbacks.beginBroadcast();
            for (int i = 0; i < n; i++) {
                try {
                    wallpaper.callbacks.getBroadcastItem(i).onWallpaperChanged();
                } catch (RemoteException e) {
                }
            }
            wallpaper.callbacks.finishBroadcast();
        }
        this.mContext.sendBroadcastAsUser(new Intent("android.intent.action.WALLPAPER_CHANGED"), new UserHandle(this.mCurrentUserId));
    }

    private void checkPermission(String permission) {
        if (this.mContext.checkCallingOrSelfPermission(permission) != 0) {
            throw new SecurityException("Access denied to process: " + Binder.getCallingPid() + ", must have permission " + permission);
        }
    }

    public boolean isWallpaperSupported(String callingPackage) {
        return this.mAppOpsManager.checkOpNoThrow(48, Binder.getCallingUid(), callingPackage) == 0;
    }

    public boolean isSetWallpaperAllowed(String callingPackage) {
        if (!Arrays.asList(this.mContext.getPackageManager().getPackagesForUid(Binder.getCallingUid())).contains(callingPackage)) {
            return false;
        }
        DevicePolicyManager dpm = (DevicePolicyManager) this.mContext.getSystemService(DevicePolicyManager.class);
        if (dpm.isDeviceOwnerApp(callingPackage) || dpm.isProfileOwnerApp(callingPackage)) {
            return true;
        }
        return true ^ ((UserManager) this.mContext.getSystemService("user")).hasUserRestriction("no_set_wallpaper");
    }

    public boolean isWallpaperBackupEligible(int which, int userId) {
        WallpaperData wallpaper;
        if (Binder.getCallingUid() == 1000) {
            if (which == 2) {
                wallpaper = this.mLockWallpaperMap.get(userId);
            } else {
                wallpaper = this.mWallpaperMap.get(userId);
            }
            if (wallpaper != null) {
                return wallpaper.allowBackup;
            }
            return false;
        }
        throw new SecurityException("Only the system may call isWallpaperBackupEligible");
    }

    private static JournaledFile makeJournaledFile(int userId) {
        String base = new File(getWallpaperDir(userId), WALLPAPER_INFO).getAbsolutePath();
        File file = new File(base);
        return new JournaledFile(file, new File(base + ".tmp"));
    }

    /* access modifiers changed from: private */
    public void saveSettingsLocked(int userId) {
        JournaledFile journal = makeJournaledFile(userId);
        try {
            XmlSerializer out = new FastXmlSerializer();
            FileOutputStream fstream = new FileOutputStream(journal.chooseForWrite(), false);
            BufferedOutputStream stream = new BufferedOutputStream(fstream);
            out.setOutput(stream, StandardCharsets.UTF_8.name());
            out.startDocument(null, true);
            WallpaperData wallpaper = this.mWallpaperMap.get(userId);
            if (wallpaper != null) {
                writeWallpaperAttributes(out, "wp", wallpaper);
            }
            WallpaperData wallpaper2 = this.mLockWallpaperMap.get(userId);
            if (wallpaper2 != null) {
                writeWallpaperAttributes(out, "kwp", wallpaper2);
            }
            out.endDocument();
            stream.flush();
            FileUtils.sync(fstream);
            stream.close();
            journal.commit();
        } catch (IOException e) {
            IoUtils.closeQuietly(null);
            journal.rollback();
        }
    }

    private void writeWallpaperAttributes(XmlSerializer out, String tag, WallpaperData wallpaper) throws IllegalArgumentException, IllegalStateException, IOException {
        out.startTag(null, tag);
        out.attribute(null, "id", Integer.toString(wallpaper.wallpaperId));
        out.attribute(null, "width", Integer.toString(wallpaper.width));
        out.attribute(null, "height", Integer.toString(wallpaper.height));
        out.attribute(null, "cropLeft", Integer.toString(wallpaper.cropHint.left));
        out.attribute(null, "cropTop", Integer.toString(wallpaper.cropHint.top));
        out.attribute(null, "cropRight", Integer.toString(wallpaper.cropHint.right));
        out.attribute(null, "cropBottom", Integer.toString(wallpaper.cropHint.bottom));
        if (wallpaper.padding.left != 0) {
            out.attribute(null, "paddingLeft", Integer.toString(wallpaper.padding.left));
        }
        if (wallpaper.padding.top != 0) {
            out.attribute(null, "paddingTop", Integer.toString(wallpaper.padding.top));
        }
        if (wallpaper.padding.right != 0) {
            out.attribute(null, "paddingRight", Integer.toString(wallpaper.padding.right));
        }
        if (wallpaper.padding.bottom != 0) {
            out.attribute(null, "paddingBottom", Integer.toString(wallpaper.padding.bottom));
        }
        if (wallpaper.primaryColors != null) {
            int colorsCount = wallpaper.primaryColors.getMainColors().size();
            out.attribute(null, "colorsCount", Integer.toString(colorsCount));
            if (colorsCount > 0) {
                for (int i = 0; i < colorsCount; i++) {
                    out.attribute(null, "colorValue" + i, Integer.toString(((Color) wallpaper.primaryColors.getMainColors().get(i)).toArgb()));
                }
            }
            out.attribute(null, "colorHints", Integer.toString(wallpaper.primaryColors.getColorHints()));
        }
        out.attribute(null, com.android.server.pm.Settings.ATTR_NAME, wallpaper.name);
        if (wallpaper.wallpaperComponent != null && !wallpaper.wallpaperComponent.equals(this.mImageWallpaper)) {
            out.attribute(null, "component", wallpaper.wallpaperComponent.flattenToShortString());
        }
        if (wallpaper.allowBackup) {
            out.attribute(null, BatteryService.HealthServiceWrapper.INSTANCE_HEALTHD, "true");
        }
        out.endTag(null, tag);
    }

    private void migrateFromOld() {
        File preNWallpaper = new File(getWallpaperDir(0), WALLPAPER_CROP);
        File originalWallpaper = new File("/data/data/com.android.settings/files/wallpaper");
        File newWallpaper = new File(getWallpaperDir(0), WALLPAPER);
        if (preNWallpaper.exists()) {
            if (!newWallpaper.exists()) {
                FileUtils.copyFile(preNWallpaper, newWallpaper);
            }
        } else if (originalWallpaper.exists()) {
            File oldInfo = new File("/data/system/wallpaper_info.xml");
            if (oldInfo.exists()) {
                oldInfo.renameTo(new File(getWallpaperDir(0), WALLPAPER_INFO));
            }
            FileUtils.copyFile(originalWallpaper, preNWallpaper);
            originalWallpaper.renameTo(newWallpaper);
        }
    }

    private int getAttributeInt(XmlPullParser parser, String name, int defValue) {
        String value = parser.getAttributeValue(null, name);
        if (value == null) {
            return defValue;
        }
        return Integer.parseInt(value);
    }

    private WallpaperData getWallpaperSafeLocked(int userId, int which) {
        SparseArray<WallpaperData> whichSet = which == 2 ? this.mLockWallpaperMap : this.mWallpaperMap;
        WallpaperData wallpaper = whichSet.get(userId);
        if (wallpaper != null) {
            return wallpaper;
        }
        loadSettingsLocked(userId, false);
        WallpaperData wallpaper2 = whichSet.get(userId);
        if (wallpaper2 != null) {
            return wallpaper2;
        }
        if (which == 2) {
            WallpaperData wallpaper3 = new WallpaperData(userId, WALLPAPER_LOCK_ORIG, WALLPAPER_LOCK_CROP);
            this.mLockWallpaperMap.put(userId, wallpaper3);
            ensureSaneWallpaperData(wallpaper3);
            return wallpaper3;
        }
        Slog.wtf(TAG, "Didn't find wallpaper in non-lock case!");
        WallpaperData wallpaper4 = new WallpaperData(userId, WALLPAPER, WALLPAPER_CROP);
        this.mWallpaperMap.put(userId, wallpaper4);
        ensureSaneWallpaperData(wallpaper4);
        return wallpaper4;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x01ba  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x01d3  */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x01ea  */
    /* JADX WARNING: Removed duplicated region for block: B:77:? A[RETURN, SYNTHETIC] */
    public void loadSettingsLocked(int userId, boolean keepDimensionHints) {
        WallpaperData lockWallpaper;
        int type;
        int i = userId;
        FileInputStream stream = null;
        File file = makeJournaledFile(userId).chooseForRead();
        WallpaperData wallpaper = this.mWallpaperMap.get(i);
        if (wallpaper == null) {
            migrateFromOld();
            wallpaper = new WallpaperData(i, WALLPAPER, WALLPAPER_CROP);
            wallpaper.allowBackup = true;
            this.mWallpaperMap.put(i, wallpaper);
            if (!wallpaper.cropExists()) {
                if (wallpaper.sourceExists()) {
                    generateCrop(wallpaper);
                } else {
                    Slog.i(TAG, "No static wallpaper imagery; defaults will be shown");
                }
            }
        }
        WallpaperData wallpaper2 = wallpaper;
        boolean success = false;
        try {
            stream = new FileInputStream(file);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream, StandardCharsets.UTF_8.name());
            do {
                type = parser.next();
                if (type == 2) {
                    String tag = parser.getName();
                    if ("wp".equals(tag)) {
                        try {
                            parseWallpaperAttributes(parser, wallpaper2, keepDimensionHints);
                            ComponentName componentName = null;
                            String comp = parser.getAttributeValue(null, "component");
                            if (comp != null) {
                                componentName = ComponentName.unflattenFromString(comp);
                            }
                            wallpaper2.nextWallpaperComponent = componentName;
                            if (wallpaper2.nextWallpaperComponent == null || PackageManagerService.PLATFORM_PACKAGE_NAME.equals(wallpaper2.nextWallpaperComponent.getPackageName())) {
                                wallpaper2.nextWallpaperComponent = this.mImageWallpaper;
                            }
                        } catch (FileNotFoundException e) {
                            Slog.w(TAG, "no current wallpaper -- first boot?");
                            this.mDefaultWallpaperComponent = WallpaperManager.getDefaultWallpaperComponent(userId);
                            Slog.w(TAG, "first boot set mDefaultWallpaperComponent=" + this.mDefaultWallpaperComponent);
                            IoUtils.closeQuietly(stream);
                            this.mSuccess = success;
                            if (!success) {
                            }
                            ensureSaneWallpaperData(wallpaper2);
                            lockWallpaper = this.mLockWallpaperMap.get(i);
                            if (lockWallpaper != null) {
                            }
                        } catch (NullPointerException e2) {
                            e = e2;
                            Slog.w(TAG, "failed parsing " + file + " " + e);
                            IoUtils.closeQuietly(stream);
                            this.mSuccess = success;
                            if (!success) {
                            }
                            ensureSaneWallpaperData(wallpaper2);
                            lockWallpaper = this.mLockWallpaperMap.get(i);
                            if (lockWallpaper != null) {
                            }
                        } catch (NumberFormatException e3) {
                            e = e3;
                            Slog.w(TAG, "failed parsing " + file + " " + e);
                            IoUtils.closeQuietly(stream);
                            this.mSuccess = success;
                            if (!success) {
                            }
                            ensureSaneWallpaperData(wallpaper2);
                            lockWallpaper = this.mLockWallpaperMap.get(i);
                            if (lockWallpaper != null) {
                            }
                        } catch (XmlPullParserException e4) {
                            e = e4;
                            Slog.w(TAG, "failed parsing " + file + " " + e);
                            IoUtils.closeQuietly(stream);
                            this.mSuccess = success;
                            if (!success) {
                            }
                            ensureSaneWallpaperData(wallpaper2);
                            lockWallpaper = this.mLockWallpaperMap.get(i);
                            if (lockWallpaper != null) {
                            }
                        } catch (IOException e5) {
                            e = e5;
                            Slog.w(TAG, "failed parsing " + file + " " + e);
                            IoUtils.closeQuietly(stream);
                            this.mSuccess = success;
                            if (!success) {
                            }
                            ensureSaneWallpaperData(wallpaper2);
                            lockWallpaper = this.mLockWallpaperMap.get(i);
                            if (lockWallpaper != null) {
                            }
                        } catch (IndexOutOfBoundsException e6) {
                            e = e6;
                            Slog.w(TAG, "failed parsing " + file + " " + e);
                            IoUtils.closeQuietly(stream);
                            this.mSuccess = success;
                            if (!success) {
                            }
                            ensureSaneWallpaperData(wallpaper2);
                            lockWallpaper = this.mLockWallpaperMap.get(i);
                            if (lockWallpaper != null) {
                            }
                        }
                    } else {
                        boolean z = keepDimensionHints;
                        if ("kwp".equals(tag)) {
                            WallpaperData lockWallpaper2 = this.mLockWallpaperMap.get(i);
                            if (lockWallpaper2 == null) {
                                lockWallpaper2 = new WallpaperData(i, WALLPAPER_LOCK_ORIG, WALLPAPER_LOCK_CROP);
                                this.mLockWallpaperMap.put(i, lockWallpaper2);
                            }
                            parseWallpaperAttributes(parser, lockWallpaper2, false);
                        }
                    }
                } else {
                    boolean z2 = keepDimensionHints;
                }
            } while (type != 1);
            success = true;
        } catch (FileNotFoundException e7) {
            boolean z3 = keepDimensionHints;
            Slog.w(TAG, "no current wallpaper -- first boot?");
            this.mDefaultWallpaperComponent = WallpaperManager.getDefaultWallpaperComponent(userId);
            Slog.w(TAG, "first boot set mDefaultWallpaperComponent=" + this.mDefaultWallpaperComponent);
            IoUtils.closeQuietly(stream);
            this.mSuccess = success;
            if (!success) {
            }
            ensureSaneWallpaperData(wallpaper2);
            lockWallpaper = this.mLockWallpaperMap.get(i);
            if (lockWallpaper != null) {
            }
        } catch (NullPointerException e8) {
            e = e8;
            boolean z4 = keepDimensionHints;
            Slog.w(TAG, "failed parsing " + file + " " + e);
            IoUtils.closeQuietly(stream);
            this.mSuccess = success;
            if (!success) {
            }
            ensureSaneWallpaperData(wallpaper2);
            lockWallpaper = this.mLockWallpaperMap.get(i);
            if (lockWallpaper != null) {
            }
        } catch (NumberFormatException e9) {
            e = e9;
            boolean z5 = keepDimensionHints;
            Slog.w(TAG, "failed parsing " + file + " " + e);
            IoUtils.closeQuietly(stream);
            this.mSuccess = success;
            if (!success) {
            }
            ensureSaneWallpaperData(wallpaper2);
            lockWallpaper = this.mLockWallpaperMap.get(i);
            if (lockWallpaper != null) {
            }
        } catch (XmlPullParserException e10) {
            e = e10;
            boolean z6 = keepDimensionHints;
            Slog.w(TAG, "failed parsing " + file + " " + e);
            IoUtils.closeQuietly(stream);
            this.mSuccess = success;
            if (!success) {
            }
            ensureSaneWallpaperData(wallpaper2);
            lockWallpaper = this.mLockWallpaperMap.get(i);
            if (lockWallpaper != null) {
            }
        } catch (IOException e11) {
            e = e11;
            boolean z7 = keepDimensionHints;
            Slog.w(TAG, "failed parsing " + file + " " + e);
            IoUtils.closeQuietly(stream);
            this.mSuccess = success;
            if (!success) {
            }
            ensureSaneWallpaperData(wallpaper2);
            lockWallpaper = this.mLockWallpaperMap.get(i);
            if (lockWallpaper != null) {
            }
        } catch (IndexOutOfBoundsException e12) {
            e = e12;
            boolean z8 = keepDimensionHints;
            Slog.w(TAG, "failed parsing " + file + " " + e);
            IoUtils.closeQuietly(stream);
            this.mSuccess = success;
            if (!success) {
            }
            ensureSaneWallpaperData(wallpaper2);
            lockWallpaper = this.mLockWallpaperMap.get(i);
            if (lockWallpaper != null) {
            }
        }
        IoUtils.closeQuietly(stream);
        this.mSuccess = success;
        if (!success) {
            wallpaper2.width = -1;
            wallpaper2.height = -1;
            wallpaper2.cropHint.set(0, 0, 0, 0);
            wallpaper2.padding.set(0, 0, 0, 0);
            wallpaper2.name = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
            this.mLockWallpaperMap.remove(i);
        } else if (wallpaper2.wallpaperId <= 0) {
            wallpaper2.wallpaperId = makeWallpaperIdLocked();
        }
        ensureSaneWallpaperData(wallpaper2);
        lockWallpaper = this.mLockWallpaperMap.get(i);
        if (lockWallpaper != null) {
            ensureSaneWallpaperData(lockWallpaper);
        }
    }

    private void ensureSaneWallpaperData(WallpaperData wallpaper) {
        int baseSize = getMaximumSizeDimension();
        if (wallpaper.width < baseSize && !getHwWallpaperWidth(wallpaper, this.mSuccess)) {
            wallpaper.width = baseSize;
        }
        if (wallpaper.height < baseSize) {
            wallpaper.height = baseSize;
        }
        if (wallpaper.cropHint.width() <= 0 || wallpaper.cropHint.height() <= 0) {
            wallpaper.cropHint.set(0, 0, wallpaper.width, wallpaper.height);
        }
    }

    private void parseWallpaperAttributes(XmlPullParser parser, WallpaperData wallpaper, boolean keepDimensionHints) {
        String idString = parser.getAttributeValue(null, "id");
        if (idString != null) {
            int id = Integer.parseInt(idString);
            wallpaper.wallpaperId = id;
            if (id > this.mWallpaperId) {
                this.mWallpaperId = id;
            }
        } else {
            wallpaper.wallpaperId = makeWallpaperIdLocked();
        }
        if (!keepDimensionHints) {
            wallpaper.width = Integer.parseInt(parser.getAttributeValue(null, "width"));
            wallpaper.height = Integer.parseInt(parser.getAttributeValue(null, "height"));
        }
        wallpaper.cropHint.left = getAttributeInt(parser, "cropLeft", 0);
        wallpaper.cropHint.top = getAttributeInt(parser, "cropTop", 0);
        wallpaper.cropHint.right = getAttributeInt(parser, "cropRight", 0);
        wallpaper.cropHint.bottom = getAttributeInt(parser, "cropBottom", 0);
        wallpaper.padding.left = getAttributeInt(parser, "paddingLeft", 0);
        wallpaper.padding.top = getAttributeInt(parser, "paddingTop", 0);
        wallpaper.padding.right = getAttributeInt(parser, "paddingRight", 0);
        wallpaper.padding.bottom = getAttributeInt(parser, "paddingBottom", 0);
        int colorsCount = getAttributeInt(parser, "colorsCount", 0);
        if (colorsCount > 0) {
            Color tertiary = null;
            Color secondary = null;
            Color primary = null;
            for (int i = 0; i < colorsCount; i++) {
                Color color = Color.valueOf(getAttributeInt(parser, "colorValue" + i, 0));
                if (i != 0) {
                    if (i != 1) {
                        if (i != 2) {
                            break;
                        }
                        tertiary = color;
                    } else {
                        secondary = color;
                    }
                } else {
                    primary = color;
                }
            }
            wallpaper.primaryColors = new WallpaperColors(primary, secondary, tertiary, getAttributeInt(parser, "colorHints", 0));
        }
        wallpaper.name = parser.getAttributeValue(null, com.android.server.pm.Settings.ATTR_NAME);
        wallpaper.allowBackup = "true".equals(parser.getAttributeValue(null, BatteryService.HealthServiceWrapper.INSTANCE_HEALTHD));
    }

    private int getMaximumSizeDimension() {
        return ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay().getMaximumSizeDimension();
    }

    public void settingsRestored() {
        WallpaperData wallpaper;
        boolean success;
        if (Binder.getCallingUid() == 1000) {
            synchronized (this.mLock) {
                loadSettingsLocked(0, false);
                wallpaper = this.mWallpaperMap.get(0);
                wallpaper.wallpaperId = makeWallpaperIdLocked();
                wallpaper.allowBackup = true;
                if (wallpaper.nextWallpaperComponent == null || wallpaper.nextWallpaperComponent.equals(this.mImageWallpaper)) {
                    if (BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS.equals(wallpaper.name)) {
                        success = true;
                    } else {
                        success = restoreNamedResourceLocked(wallpaper);
                    }
                    if (success) {
                        generateCrop(wallpaper);
                        bindWallpaperComponentLocked(wallpaper.nextWallpaperComponent, true, false, wallpaper, null);
                    }
                } else {
                    if (!bindWallpaperComponentLocked(wallpaper.nextWallpaperComponent, false, false, wallpaper, null)) {
                        bindWallpaperComponentLocked(null, false, false, wallpaper, null);
                    }
                    success = true;
                }
            }
            if (!success) {
                Slog.e(TAG, "Failed to restore wallpaper: '" + wallpaper.name + "'");
                wallpaper.name = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
                getWallpaperDir(0).delete();
            }
            synchronized (this.mLock) {
                saveSettingsLocked(0);
            }
            return;
        }
        throw new RuntimeException("settingsRestored() can only be called from the system process");
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x0127, code lost:
        if (0 != 0) goto L_0x0129;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0129, code lost:
        android.os.FileUtils.sync(null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x012c, code lost:
        libcore.io.IoUtils.closeQuietly(null);
        libcore.io.IoUtils.closeQuietly(null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x0152, code lost:
        if (0 != 0) goto L_0x0129;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x0179, code lost:
        if (0 != 0) goto L_0x0129;
     */
    public boolean restoreNamedResourceLocked(WallpaperData wallpaper) {
        WallpaperData wallpaperData = wallpaper;
        if (wallpaperData.name.length() > 4 && "res:".equals(wallpaperData.name.substring(0, 4))) {
            String resName = wallpaperData.name.substring(4);
            String pkg = null;
            int colon = resName.indexOf(58);
            if (colon > 0) {
                pkg = resName.substring(0, colon);
            }
            String pkg2 = pkg;
            String ident = null;
            int slash = resName.lastIndexOf(47);
            if (slash > 0) {
                ident = resName.substring(slash + 1);
            }
            String ident2 = ident;
            String type = null;
            if (colon > 0 && slash > 0 && slash - colon > 1) {
                type = resName.substring(colon + 1, slash);
            }
            String type2 = type;
            if (!(pkg2 == null || ident2 == null || type2 == null)) {
                int resId = -1;
                try {
                    Context c = this.mContext.createPackageContext(pkg2, 4);
                    Resources r = c.getResources();
                    resId = r.getIdentifier(resName, null, null);
                    if (resId == 0) {
                        StringBuilder sb = new StringBuilder();
                        Context context = c;
                        sb.append("couldn't resolve identifier pkg=");
                        sb.append(pkg2);
                        sb.append(" type=");
                        sb.append(type2);
                        sb.append(" ident=");
                        sb.append(ident2);
                        Slog.e(TAG, sb.toString());
                        IoUtils.closeQuietly(null);
                        if (0 != 0) {
                            FileUtils.sync(null);
                        }
                        if (0 != 0) {
                            FileUtils.sync(null);
                        }
                        IoUtils.closeQuietly(null);
                        IoUtils.closeQuietly(null);
                        return false;
                    }
                    InputStream res = r.openRawResource(resId);
                    if (wallpaperData.wallpaperFile.exists()) {
                        wallpaperData.wallpaperFile.delete();
                        wallpaperData.cropFile.delete();
                    }
                    FileOutputStream fos = new FileOutputStream(wallpaperData.wallpaperFile);
                    FileOutputStream cos = new FileOutputStream(wallpaperData.cropFile);
                    byte[] buffer = new byte[32768];
                    while (true) {
                        int read = res.read(buffer);
                        int amt = read;
                        if (read > 0) {
                            fos.write(buffer, 0, amt);
                            cos.write(buffer, 0, amt);
                        } else {
                            byte[] bArr = buffer;
                            Slog.v(TAG, "Restored wallpaper: " + resName);
                            IoUtils.closeQuietly(res);
                            FileUtils.sync(fos);
                            FileUtils.sync(cos);
                            IoUtils.closeQuietly(fos);
                            IoUtils.closeQuietly(cos);
                            return true;
                        }
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Slog.e(TAG, "Package name " + pkg2 + " not found");
                    IoUtils.closeQuietly(null);
                    if (0 != 0) {
                        FileUtils.sync(null);
                    }
                } catch (Resources.NotFoundException e2) {
                    Slog.e(TAG, "Resource not found: " + resId);
                    IoUtils.closeQuietly(null);
                    if (0 != 0) {
                        FileUtils.sync(null);
                    }
                } catch (IOException e3) {
                    Slog.e(TAG, "IOException while restoring wallpaper ", e3);
                    IoUtils.closeQuietly(null);
                    if (0 != 0) {
                        FileUtils.sync(null);
                    }
                } catch (Throwable th) {
                    IoUtils.closeQuietly(null);
                    if (0 != 0) {
                        FileUtils.sync(null);
                    }
                    if (0 != 0) {
                        FileUtils.sync(null);
                    }
                    IoUtils.closeQuietly(null);
                    IoUtils.closeQuietly(null);
                    throw th;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, pw)) {
            synchronized (this.mLock) {
                pw.println("System wallpaper state:");
                for (int i = 0; i < this.mWallpaperMap.size(); i++) {
                    WallpaperData wallpaper = this.mWallpaperMap.valueAt(i);
                    pw.print(" User ");
                    pw.print(wallpaper.userId);
                    pw.print(": id=");
                    pw.println(wallpaper.wallpaperId);
                    pw.print("  mWidth=");
                    pw.print(wallpaper.width);
                    pw.print(" mHeight=");
                    pw.println(wallpaper.height);
                    pw.print("  mCropHint=");
                    pw.println(wallpaper.cropHint);
                    pw.print("  mPadding=");
                    pw.println(wallpaper.padding);
                    pw.print("  mName=");
                    pw.println(wallpaper.name);
                    pw.print("  mAllowBackup=");
                    pw.println(wallpaper.allowBackup);
                    pw.print("  mWallpaperComponent=");
                    pw.println(wallpaper.wallpaperComponent);
                    if (wallpaper.connection != null) {
                        WallpaperConnection conn = wallpaper.connection;
                        pw.print("  Wallpaper connection ");
                        pw.print(conn);
                        pw.println(":");
                        if (conn.mInfo != null) {
                            pw.print("    mInfo.component=");
                            pw.println(conn.mInfo.getComponent());
                        }
                        pw.print("    mToken=");
                        pw.println(conn.mToken);
                        pw.print("    mService=");
                        pw.println(conn.mService);
                        pw.print("    mEngine=");
                        pw.println(conn.mEngine);
                        pw.print("    mLastDiedTime=");
                        pw.println(wallpaper.lastDiedTime - SystemClock.uptimeMillis());
                    }
                }
                pw.println("Lock wallpaper state:");
                for (int i2 = 0; i2 < this.mLockWallpaperMap.size(); i2++) {
                    WallpaperData wallpaper2 = this.mLockWallpaperMap.valueAt(i2);
                    pw.print(" User ");
                    pw.print(wallpaper2.userId);
                    pw.print(": id=");
                    pw.println(wallpaper2.wallpaperId);
                    pw.print("  mWidth=");
                    pw.print(wallpaper2.width);
                    pw.print(" mHeight=");
                    pw.println(wallpaper2.height);
                    pw.print("  mCropHint=");
                    pw.println(wallpaper2.cropHint);
                    pw.print("  mPadding=");
                    pw.println(wallpaper2.padding);
                    pw.print("  mName=");
                    pw.println(wallpaper2.name);
                    pw.print("  mAllowBackup=");
                    pw.println(wallpaper2.allowBackup);
                }
            }
        }
    }

    public void handleWallpaperObserverEvent(WallpaperData wallpaper) {
    }

    public ParcelFileDescriptor getBlurWallpaper(IWallpaperManagerCallback cb) {
        return null;
    }

    /* access modifiers changed from: protected */
    public SparseArray<WallpaperData> getWallpaperMap() {
        return this.mWallpaperMap;
    }

    /* access modifiers changed from: protected */
    public Context getContext() {
        return this.mContext;
    }

    /* access modifiers changed from: protected */
    public Object getLock() {
        return this.mLock;
    }

    /* access modifiers changed from: protected */
    public int getCurrentUserId() {
        return this.mCurrentUserId;
    }

    /* access modifiers changed from: protected */
    public boolean getHwWallpaperWidth(WallpaperData wallpaper, boolean success) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void updateWallpaperOffsets(WallpaperData wallpaper) {
    }

    public int[] getCurrOffsets() throws RemoteException {
        return null;
    }

    public void setCurrOffsets(int[] offsets) throws RemoteException {
    }

    public void setNextOffsets(int[] offsets) throws RemoteException {
    }

    public int getWallpaperUserId() {
        return this.mCurrentUserId;
    }

    public Bitmap scaleWallpaperBitmapToScreenSize(Bitmap bitmap) {
        return bitmap;
    }

    public boolean isMdmDisableChangeWallpaper() {
        if (!HwDeviceManager.disallowOp(35)) {
            return false;
        }
        Slog.v(TAG, "WallpaperManagerService:MDM policy forbidden setWallpaper");
        mHandler.postDelayed(new Runnable() {
            public void run() {
                Toast toast = Toast.makeText(WallpaperManagerService.this.mContext, 33685955, 0);
                toast.getWindowParams().type = 2010;
                toast.getWindowParams().privateFlags |= 16;
                toast.show();
            }
        }, 300);
        return true;
    }

    public void reportWallpaper(ComponentName componentName) {
    }
}
