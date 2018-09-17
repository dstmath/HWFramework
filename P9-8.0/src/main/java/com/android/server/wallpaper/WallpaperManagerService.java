package com.android.server.wallpaper;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.IWallpaperManager.Stub;
import android.app.IWallpaperManagerCallback;
import android.app.PendingIntent;
import android.app.UserSwitchObserver;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
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
import android.os.IRemoteCallback;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SELinux;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.service.wallpaper.IWallpaperConnection;
import android.service.wallpaper.IWallpaperEngine;
import android.service.wallpaper.IWallpaperService;
import android.system.ErrnoException;
import android.system.Os;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.Slog;
import android.util.SparseArray;
import android.util.Xml;
import android.view.IWindowManager;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;
import com.android.internal.content.PackageMonitor;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.JournaledFile;
import com.android.server.EventLogTags;
import com.android.server.FgThread;
import com.android.server.SystemService;
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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class WallpaperManagerService extends Stub {
    static final boolean DEBUG = false;
    static final boolean DEBUG_LIVE = true;
    private static final String HUAWEI_LAUNCHER_PACKAGE = "com.huawei.android.launcher";
    private static final String HUAWEI_SYSTEMUI_PACKAGE = "com.android.systemui";
    static final int MAX_WALLPAPER_COMPONENT_LOG_LENGTH = 128;
    static final long MIN_WALLPAPER_CRASH_TIME = 10000;
    private static final String NOTCH_PROP = SystemProperties.get("ro.config.hw_notch_size", "");
    static final String TAG = "WallpaperManagerService";
    static final String WALLPAPER = "wallpaper_orig";
    static final String WALLPAPER_CROP = "wallpaper";
    static final String WALLPAPER_INFO = "wallpaper_info.xml";
    static final String WALLPAPER_LOCK_CROP = "wallpaper_lock";
    static final String WALLPAPER_LOCK_ORIG = "wallpaper_lock_orig";
    public static Handler mHandler = new Handler(Looper.getMainLooper());
    static final String[] sPerUserFiles = new String[]{WALLPAPER, WALLPAPER_CROP, WALLPAPER_LOCK_ORIG, WALLPAPER_LOCK_CROP, WALLPAPER_INFO};
    final AppOpsManager mAppOpsManager;
    final Context mContext;
    int mCurrentUserId;
    final ComponentName mDefaultWallpaperComponent;
    final IPackageManager mIPackageManager;
    final IWindowManager mIWindowManager;
    final ComponentName mImageWallpaper;
    IWallpaperManagerCallback mKeyguardListener;
    WallpaperData mLastWallpaper;
    final Object mLock = new Object();
    final SparseArray<WallpaperData> mLockWallpaperMap = new SparseArray();
    final MyPackageMonitor mMonitor;
    boolean mShuttingDown;
    boolean mSuccess = false;
    final SparseArray<Boolean> mUserRestorecon = new SparseArray();
    boolean mWaitingForUnlock;
    int mWallpaperId;
    final SparseArray<WallpaperData> mWallpaperMap = new SparseArray();

    public static class Lifecycle extends SystemService {
        private WallpaperManagerService mService;

        public Lifecycle(Context context) {
            super(context);
        }

        public void onStart() {
            this.mService = new WallpaperManagerService(getContext());
            publishBinderService(WallpaperManagerService.WALLPAPER_CROP, this.mService);
        }

        public void onBootPhase(int phase) {
            if (phase == 550) {
                this.mService.systemReady();
            } else if (phase == 600) {
                this.mService.switchUser(0, null);
            }
        }

        public void onUnlockUser(int userHandle) {
            this.mService.onUnlockUser(userHandle);
        }
    }

    class MyPackageMonitor extends PackageMonitor {
        static final String PACKAGE_SYSTEMUI = "com.android.systemui";

        MyPackageMonitor() {
        }

        /* JADX WARNING: Missing block: B:18:0x008f, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onPackageUpdateFinished(String packageName, int uid) {
            synchronized (WallpaperManagerService.this.mLock) {
                if (WallpaperManagerService.this.mCurrentUserId != getChangingUserId()) {
                    return;
                }
                WallpaperData wallpaper = (WallpaperData) WallpaperManagerService.this.mWallpaperMap.get(WallpaperManagerService.this.mCurrentUserId);
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

        /* JADX WARNING: Missing block: B:16:0x0034, code:
            return;
     */
        /* JADX WARNING: Missing block: B:21:0x003a, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onPackageModified(String packageName) {
            synchronized (WallpaperManagerService.this.mLock) {
                if (WallpaperManagerService.this.mCurrentUserId != getChangingUserId()) {
                    return;
                }
                WallpaperData wallpaper = (WallpaperData) WallpaperManagerService.this.mWallpaperMap.get(WallpaperManagerService.this.mCurrentUserId);
                if (wallpaper != null) {
                    if (wallpaper.wallpaperComponent == null || (wallpaper.wallpaperComponent.getPackageName().equals(packageName) ^ 1) != 0) {
                    } else {
                        doPackagesChangedLocked(true, wallpaper);
                    }
                }
            }
        }

        /* JADX WARNING: Missing block: B:18:0x0069, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onPackageUpdateStarted(String packageName, int uid) {
            synchronized (WallpaperManagerService.this.mLock) {
                if (WallpaperManagerService.this.mCurrentUserId != getChangingUserId()) {
                    return;
                }
                WallpaperData wallpaper = (WallpaperData) WallpaperManagerService.this.mWallpaperMap.get(WallpaperManagerService.this.mCurrentUserId);
                if (!(wallpaper == null || wallpaper.wallpaperComponent == null || !wallpaper.wallpaperComponent.getPackageName().equals(packageName))) {
                    Slog.i(WallpaperManagerService.TAG, "Wallpaper service " + wallpaper.wallpaperComponent + " is updating");
                    wallpaper.wallpaperUpdating = true;
                    if (wallpaper.connection != null) {
                        FgThread.getHandler().removeCallbacks(wallpaper.connection.mResetRunnable);
                    }
                }
            }
        }

        /* JADX WARNING: Missing block: B:19:0x0041, code:
            return r0;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean onHandleForceStop(Intent intent, String[] packages, int uid, boolean doit) {
            int i = 0;
            synchronized (WallpaperManagerService.this.mLock) {
                boolean changed = false;
                if (WallpaperManagerService.this.mCurrentUserId != getChangingUserId()) {
                    return false;
                }
                int length = packages.length;
                while (i < length) {
                    if (PACKAGE_SYSTEMUI.equals(packages[i])) {
                        doit = false;
                        Slog.w(WallpaperManagerService.TAG, "SystemUI has been forced to stop!");
                        break;
                    }
                    i++;
                }
                WallpaperData wallpaper = (WallpaperData) WallpaperManagerService.this.mWallpaperMap.get(WallpaperManagerService.this.mCurrentUserId);
                if (wallpaper != null) {
                    changed = doPackagesChangedLocked(doit, wallpaper);
                }
            }
        }

        /* JADX WARNING: Missing block: B:12:0x0026, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onSomePackagesChanged() {
            synchronized (WallpaperManagerService.this.mLock) {
                if (WallpaperManagerService.this.mCurrentUserId != getChangingUserId()) {
                    return;
                }
                WallpaperData wallpaper = (WallpaperData) WallpaperManagerService.this.mWallpaperMap.get(WallpaperManagerService.this.mCurrentUserId);
                if (wallpaper != null) {
                    doPackagesChangedLocked(true, wallpaper);
                }
            }
        }

        boolean doPackagesChangedLocked(boolean doit, WallpaperData wallpaper) {
            int change;
            boolean changed = false;
            if (wallpaper.wallpaperComponent != null) {
                change = isPackageDisappearing(wallpaper.wallpaperComponent.getPackageName());
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
                change = isPackageDisappearing(wallpaper.nextWallpaperComponent.getPackageName());
                if (change == 3 || change == 2) {
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

    class WallpaperConnection extends IWallpaperConnection.Stub implements ServiceConnection {
        private static final long WALLPAPER_RECONNECT_TIMEOUT_MS = 10000;
        boolean mDimensionsChanged = false;
        IWallpaperEngine mEngine;
        final WallpaperInfo mInfo;
        boolean mPaddingChanged = false;
        IRemoteCallback mReply;
        private Runnable mResetRunnable = new -$Lambda$ZWcNEw3ZwVVSi_pP2mGGLvztkS0(this);
        IWallpaperService mService;
        final Binder mToken = new Binder();
        WallpaperData mWallpaper;

        /* JADX WARNING: Missing block: B:15:0x0058, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        /* synthetic */ void lambda$-com_android_server_wallpaper_WallpaperManagerService$WallpaperConnection_28675() {
            synchronized (WallpaperManagerService.this.mLock) {
                if (WallpaperManagerService.this.mShuttingDown) {
                    Slog.i(WallpaperManagerService.TAG, "Ignoring relaunch timeout during shutdown");
                } else if (!this.mWallpaper.wallpaperUpdating && this.mWallpaper.userId == WallpaperManagerService.this.mCurrentUserId) {
                    Slog.w(WallpaperManagerService.TAG, "Wallpaper reconnect timed out for " + this.mWallpaper.wallpaperComponent + ", reverting to built-in wallpaper!");
                    WallpaperManagerService.this.clearWallpaperLocked(true, 1, this.mWallpaper.userId, null);
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
                    WallpaperManagerService.this.mContext.getMainThreadHandler().postDelayed(new com.android.server.wallpaper.-$Lambda$ZWcNEw3ZwVVSi_pP2mGGLvztkS0.AnonymousClass1(this), 1000);
                }
            }
        }

        private void processDisconnect(ServiceConnection connection) {
            synchronized (WallpaperManagerService.this.mLock) {
                if (connection == this.mWallpaper.connection) {
                    ComponentName wpService = this.mWallpaper.wallpaperComponent;
                    if (!(this.mWallpaper.wallpaperUpdating || this.mWallpaper.userId != WallpaperManagerService.this.mCurrentUserId || (Objects.equals(WallpaperManagerService.this.mDefaultWallpaperComponent, wpService) ^ 1) == 0 || (Objects.equals(WallpaperManagerService.this.mImageWallpaper, wpService) ^ 1) == 0)) {
                        if (this.mWallpaper.lastDiedTime == 0 || this.mWallpaper.lastDiedTime + 10000 <= SystemClock.uptimeMillis()) {
                            this.mWallpaper.lastDiedTime = SystemClock.uptimeMillis();
                            Handler fgHandler = FgThread.getHandler();
                            fgHandler.removeCallbacks(this.mResetRunnable);
                            fgHandler.postDelayed(this.mResetRunnable, 10000);
                            Slog.i(WallpaperManagerService.TAG, "Started wallpaper reconnect timeout for " + wpService);
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
            return;
        }

        public ParcelFileDescriptor setWallpaper(String name) {
            synchronized (WallpaperManagerService.this.mLock) {
                if (this.mWallpaper.connection == this) {
                    ParcelFileDescriptor updateWallpaperBitmapLocked = WallpaperManagerService.this.updateWallpaperBitmapLocked(name, this.mWallpaper, null);
                    return updateWallpaperBitmapLocked;
                }
                return null;
            }
        }
    }

    public static class WallpaperData {
        boolean allowBackup;
        private RemoteCallbackList<IWallpaperManagerCallback> callbacks = new RemoteCallbackList();
        WallpaperConnection connection;
        final File cropFile;
        final Rect cropHint = new Rect(0, 0, 0, 0);
        int[] currOffsets = new int[]{-1, -1, -1, -1};
        int height = -1;
        boolean imageWallpaperPending;
        long lastDiedTime;
        String name = "";
        int[] nextOffsets = new int[]{-1, -1, -1, -1};
        ComponentName nextWallpaperComponent;
        final Rect padding = new Rect(0, 0, 0, 0);
        IWallpaperManagerCallback setComplete;
        int userId;
        ComponentName wallpaperComponent;
        final File wallpaperFile;
        int wallpaperId;
        WallpaperObserver wallpaperObserver;
        boolean wallpaperUpdating;
        int whichPending;
        int width = -1;

        WallpaperData(int userId, String inputFileName, String cropFileName) {
            this.userId = userId;
            File wallpaperDir = WallpaperManagerService.getWallpaperDir(userId);
            this.wallpaperFile = new File(wallpaperDir, inputFileName);
            this.cropFile = new File(wallpaperDir, cropFileName);
        }

        boolean cropExists() {
            return this.cropFile.exists();
        }

        boolean sourceExists() {
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
                    wallpaper = (WallpaperData) WallpaperManagerService.this.mLockWallpaperMap.get(this.mUserId);
                }
                if (wallpaper == null) {
                    wallpaper = (WallpaperData) WallpaperManagerService.this.mWallpaperMap.get(this.mUserId);
                }
            }
            if (wallpaper != null) {
                return wallpaper;
            }
            return this.mWallpaper;
        }

        /* JADX WARNING: Missing block: B:45:0x00ab, code:
            if (r4.imageWallpaperPending != false) goto L_0x0057;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onEvent(int event, String path) {
            if (path != null) {
                boolean moved = event == 128;
                boolean written = event != 8 ? moved : true;
                File changedFile = new File(this.mWallpaperDir, path);
                boolean sysWallpaperChanged = this.mWallpaperFile.equals(changedFile);
                boolean lockWallpaperChanged = this.mWallpaperLockFile.equals(changedFile);
                WallpaperData wallpaper = dataForEvent(sysWallpaperChanged, lockWallpaperChanged);
                if (moved && lockWallpaperChanged) {
                    SELinux.restorecon(changedFile);
                    WallpaperManagerService.this.notifyLockWallpaperChanged();
                    return;
                }
                synchronized (WallpaperManagerService.this.mLock) {
                    if (sysWallpaperChanged || lockWallpaperChanged) {
                        WallpaperManagerService.this.updateWallpaperOffsets(this.mWallpaper);
                        WallpaperManagerService.this.handleWallpaperObserverEvent(this.mWallpaper);
                        WallpaperManagerService.this.notifyCallbacksLocked(wallpaper);
                        if (wallpaper.wallpaperComponent != null && event == 8) {
                        }
                        if (written) {
                            SELinux.restorecon(changedFile);
                            if (moved) {
                                SELinux.restorecon(changedFile);
                                WallpaperManagerService.this.loadSettingsLocked(wallpaper.userId, true);
                            }
                            WallpaperManagerService.this.generateCrop(wallpaper);
                            wallpaper.imageWallpaperPending = false;
                            if (wallpaper.setComplete != null) {
                                try {
                                    wallpaper.setComplete.onWallpaperChanged();
                                } catch (RemoteException e) {
                                }
                            }
                            if (sysWallpaperChanged) {
                                WallpaperManagerService.this.bindWallpaperComponentLocked(WallpaperManagerService.this.mImageWallpaper, false, false, wallpaper, null);
                            }
                            if (lockWallpaperChanged || (wallpaper.whichPending & 2) != 0) {
                                if (!lockWallpaperChanged) {
                                    WallpaperManagerService.this.mLockWallpaperMap.remove(wallpaper.userId);
                                }
                                WallpaperManagerService.this.notifyLockWallpaperChanged();
                            }
                            WallpaperManagerService.this.saveSettingsLocked(wallpaper.userId);
                        }
                    }
                }
            }
        }
    }

    void notifyLockWallpaperChanged() {
        IWallpaperManagerCallback cb = this.mKeyguardListener;
        if (cb != null) {
            try {
                cb.onWallpaperChanged();
            } catch (RemoteException e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:6:0x004a  */
    /* JADX WARNING: Removed duplicated region for block: B:91:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x0068  */
    /* JADX WARNING: Removed duplicated region for block: B:6:0x004a  */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x0068  */
    /* JADX WARNING: Removed duplicated region for block: B:91:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:6:0x004a  */
    /* JADX WARNING: Removed duplicated region for block: B:91:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x0068  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void generateCrop(WallpaperData wallpaper) {
        Throwable th;
        boolean success = false;
        Rect cropHint = new Rect(wallpaper.cropHint);
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(wallpaper.wallpaperFile.getAbsolutePath(), options);
        if (options.outWidth <= 0 || options.outHeight <= 0) {
            Slog.w(TAG, "Invalid wallpaper data");
            success = false;
        } else {
            boolean needCrop = false;
            if (cropHint.isEmpty()) {
                cropHint.top = 0;
                cropHint.left = 0;
                cropHint.right = options.outWidth;
                cropHint.bottom = options.outHeight;
            } else {
                cropHint.offset(cropHint.right > options.outWidth ? options.outWidth - cropHint.right : 0, cropHint.bottom > options.outHeight ? options.outHeight - cropHint.bottom : 0);
                if (cropHint.left < 0) {
                    cropHint.left = 0;
                }
                if (cropHint.top < 0) {
                    cropHint.top = 0;
                }
                if (options.outHeight <= cropHint.height()) {
                    if (options.outWidth > cropHint.width()) {
                        needCrop = true;
                    } else {
                        needCrop = false;
                    }
                } else {
                    needCrop = true;
                }
            }
            boolean needScale = wallpaper.height != (cropHint.height() > cropHint.width() ? cropHint.height() : cropHint.width());
            if (needScale && wallpaper.width == cropHint.width() && wallpaper.height == cropHint.height()) {
                needScale = false;
                Slog.v(TAG, "same size wallpaper ,not need to scale.  w=" + cropHint.width() + " h=" + cropHint.height());
            }
            if (needCrop || (needScale ^ 1) == 0) {
                AutoCloseable f = null;
                BufferedOutputStream bos = null;
                try {
                    Options scaler;
                    BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(wallpaper.wallpaperFile.getAbsolutePath(), false);
                    int scale = 1;
                    while (scale * 2 < cropHint.height() / wallpaper.height) {
                        scale *= 2;
                    }
                    if (scale > 1) {
                        scaler = new Options();
                        scaler.inSampleSize = scale;
                    } else {
                        scaler = null;
                    }
                    Bitmap cropped = decoder.decodeRegion(cropHint, scaler);
                    decoder.recycle();
                    if (cropped == null) {
                        Slog.e(TAG, "Could not decode new wallpaper");
                    } else {
                        cropHint.offsetTo(0, 0);
                        cropHint.right /= scale;
                        cropHint.bottom /= scale;
                        Bitmap finalCrop = Bitmap.createScaledBitmap(cropped, (int) (((float) cropHint.width()) * (((float) wallpaper.height) / ((float) cropHint.height()))), wallpaper.height, true);
                        FileOutputStream f2 = new FileOutputStream(wallpaper.cropFile);
                        Object f3;
                        try {
                            BufferedOutputStream bos2 = new BufferedOutputStream(f2, 32768);
                            try {
                                finalCrop.compress(CompressFormat.JPEG, 100, bos2);
                                bos2.flush();
                                success = true;
                                bos = bos2;
                                f3 = f2;
                            } catch (Exception e) {
                                bos = bos2;
                                f3 = f2;
                                IoUtils.closeQuietly(bos);
                                IoUtils.closeQuietly(f);
                                if (!success) {
                                }
                                if (wallpaper.cropFile.exists()) {
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                bos = bos2;
                                f3 = f2;
                                IoUtils.closeQuietly(bos);
                                IoUtils.closeQuietly(f);
                                throw th;
                            }
                        } catch (Exception e2) {
                            f3 = f2;
                            IoUtils.closeQuietly(bos);
                            IoUtils.closeQuietly(f);
                            if (success) {
                            }
                            if (wallpaper.cropFile.exists()) {
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            f3 = f2;
                            IoUtils.closeQuietly(bos);
                            IoUtils.closeQuietly(f);
                            throw th;
                        }
                    }
                    IoUtils.closeQuietly(bos);
                    IoUtils.closeQuietly(f);
                } catch (Exception e3) {
                    IoUtils.closeQuietly(bos);
                    IoUtils.closeQuietly(f);
                    if (success) {
                    }
                    if (wallpaper.cropFile.exists()) {
                    }
                } catch (Throwable th4) {
                    th = th4;
                    IoUtils.closeQuietly(bos);
                    IoUtils.closeQuietly(f);
                    throw th;
                }
            }
            success = FileUtils.copyFile(wallpaper.wallpaperFile, wallpaper.cropFile);
            if (!success) {
                wallpaper.cropFile.delete();
            }
        }
        if (success) {
            Slog.e(TAG, "Unable to apply new wallpaper");
            wallpaper.cropFile.delete();
        }
        if (wallpaper.cropFile.exists()) {
            boolean restorecon = SELinux.restorecon(wallpaper.cropFile.getAbsoluteFile());
        }
    }

    int makeWallpaperIdLocked() {
        do {
            this.mWallpaperId++;
        } while (this.mWallpaperId == 0);
        return this.mWallpaperId;
    }

    public WallpaperManagerService(Context context) {
        this.mContext = context;
        this.mShuttingDown = false;
        this.mImageWallpaper = ComponentName.unflattenFromString(context.getResources().getString(17040138));
        this.mDefaultWallpaperComponent = WallpaperManager.getDefaultWallpaperComponent(context);
        this.mIWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
        this.mIPackageManager = AppGlobals.getPackageManager();
        this.mAppOpsManager = (AppOpsManager) this.mContext.getSystemService("appops");
        this.mMonitor = new MyPackageMonitor();
        this.mMonitor.register(context, null, UserHandle.ALL, true);
        getWallpaperDir(0).mkdirs();
        loadSettingsLocked(0, false);
    }

    private static File getWallpaperDir(int userId) {
        return Environment.getUserSystemDirectory(userId);
    }

    protected void finalize() throws Throwable {
        super.finalize();
        for (int i = 0; i < this.mWallpaperMap.size(); i++) {
            ((WallpaperData) this.mWallpaperMap.valueAt(i)).wallpaperObserver.stopWatching();
        }
    }

    void systemReady() {
        WallpaperData wallpaper = (WallpaperData) this.mWallpaperMap.get(0);
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
        if (Binder.getCallingUid() != 1000) {
            throw new RuntimeException("getName() can only be called from the system process");
        }
        String str;
        synchronized (this.mLock) {
            str = ((WallpaperData) this.mWallpaperMap.get(0)).name;
        }
        return str;
    }

    void stopObserver(WallpaperData wallpaper) {
        if (wallpaper != null && wallpaper.wallpaperObserver != null) {
            wallpaper.wallpaperObserver.stopWatching();
            wallpaper.wallpaperObserver = null;
        }
    }

    void stopObserversLocked(int userId) {
        stopObserver((WallpaperData) this.mWallpaperMap.get(userId));
        stopObserver((WallpaperData) this.mLockWallpaperMap.get(userId));
        this.mWallpaperMap.remove(userId);
        this.mLockWallpaperMap.remove(userId);
    }

    void onUnlockUser(final int userId) {
        synchronized (this.mLock) {
            if (this.mCurrentUserId == userId) {
                if (this.mWaitingForUnlock) {
                    switchUser(userId, null);
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

    void onRemoveUser(int userId) {
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

    void switchUser(int userId, IRemoteCallback reply) {
        synchronized (this.mLock) {
            this.mCurrentUserId = userId;
            if (this.mLastWallpaper != null) {
                handleWallpaperObserverEvent(this.mLastWallpaper);
            }
            WallpaperData wallpaper = getWallpaperSafeLocked(userId, 1);
            if (wallpaper.wallpaperObserver == null) {
                wallpaper.wallpaperObserver = new WallpaperObserver(wallpaper);
                wallpaper.wallpaperObserver.startWatching();
            }
            switchWallpaper(wallpaper, reply);
        }
    }

    void switchWallpaper(WallpaperData wallpaper, IRemoteCallback reply) {
        synchronized (this.mLock) {
            this.mWaitingForUnlock = false;
            ComponentName cname = wallpaper.wallpaperComponent != null ? wallpaper.wallpaperComponent : wallpaper.nextWallpaperComponent;
            if (!bindWallpaperComponentLocked(cname, true, false, wallpaper, reply)) {
                ServiceInfo si = null;
                try {
                    si = this.mIPackageManager.getServiceInfo(cname, DumpState.DUMP_DOMAIN_PREFERRED, wallpaper.userId);
                } catch (RemoteException e) {
                }
                if (si == null) {
                    Slog.w(TAG, "Failure starting previous wallpaper; clearing");
                    clearWallpaperLocked(false, 1, wallpaper.userId, reply);
                } else {
                    Slog.w(TAG, "Wallpaper isn't direct boot aware; using fallback until unlocked");
                    wallpaper.wallpaperComponent = wallpaper.nextWallpaperComponent;
                    WallpaperData fallback = new WallpaperData(wallpaper.userId, WALLPAPER_LOCK_ORIG, WALLPAPER_LOCK_CROP);
                    ensureSaneWallpaperData(fallback);
                    bindWallpaperComponentLocked(this.mImageWallpaper, true, false, fallback, reply);
                    this.mWaitingForUnlock = true;
                }
            }
        }
    }

    public void clearWallpaper(String callingPackage, int which, int userId) {
        checkPermission("android.permission.SET_WALLPAPER");
        if (isWallpaperSupported(callingPackage) && (isSetWallpaperAllowed(callingPackage) ^ 1) == 0) {
            userId = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, true, "clearWallpaper", null);
            synchronized (this.mLock) {
                clearWallpaperLocked(false, which, userId, null);
            }
        }
    }

    void clearWallpaperLocked(boolean defaultFailed, int which, int userId, IRemoteCallback reply) {
        if (which == 1 || which == 2) {
            WallpaperData wallpaper;
            if (which == 2) {
                wallpaper = (WallpaperData) this.mLockWallpaperMap.get(userId);
                if (wallpaper == null) {
                    return;
                }
            }
            wallpaper = (WallpaperData) this.mWallpaperMap.get(userId);
            if (wallpaper == null) {
                loadSettingsLocked(userId, false);
                wallpaper = (WallpaperData) this.mWallpaperMap.get(userId);
            }
            if (wallpaper != null) {
                long ident = Binder.clearCallingIdentity();
                try {
                    if (wallpaper.wallpaperFile.exists()) {
                        wallpaper.wallpaperFile.delete();
                        wallpaper.cropFile.delete();
                        if (which == 2) {
                            this.mLockWallpaperMap.remove(userId);
                            IWallpaperManagerCallback cb = this.mKeyguardListener;
                            if (cb != null) {
                                try {
                                    cb.onWallpaperChanged();
                                } catch (RemoteException e) {
                                }
                            }
                            saveSettingsLocked(userId);
                            return;
                        }
                    }
                    Throwable e2 = null;
                    try {
                        wallpaper.imageWallpaperPending = false;
                        if (userId != this.mCurrentUserId) {
                            Binder.restoreCallingIdentity(ident);
                            return;
                        }
                        ComponentName componentName;
                        if (defaultFailed) {
                            componentName = this.mImageWallpaper;
                        } else {
                            componentName = null;
                        }
                        if (bindWallpaperComponentLocked(componentName, true, false, wallpaper, reply)) {
                            Binder.restoreCallingIdentity(ident);
                            return;
                        }
                        Slog.e(TAG, "Default wallpaper component not found!", e2);
                        clearWallpaperComponentLocked(wallpaper);
                        if (reply != null) {
                            try {
                                reply.sendResult(null);
                            } catch (RemoteException e3) {
                            }
                        }
                        Binder.restoreCallingIdentity(ident);
                        return;
                    } catch (Throwable e1) {
                        e2 = e1;
                    }
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                return;
            }
        }
        throw new IllegalArgumentException("Must specify exactly one kind of wallpaper to read");
    }

    public boolean hasNamedWallpaper(String name) {
        synchronized (this.mLock) {
            long ident = Binder.clearCallingIdentity();
            try {
                List<UserInfo> users = ((UserManager) this.mContext.getSystemService("user")).getUsers();
                Binder.restoreCallingIdentity(ident);
                for (UserInfo user : users) {
                    if (!user.isManagedProfile()) {
                        WallpaperData wd = (WallpaperData) this.mWallpaperMap.get(user.id);
                        if (wd == null) {
                            loadSettingsLocked(user.id, false);
                            wd = (WallpaperData) this.mWallpaperMap.get(user.id);
                        }
                        if (wd != null && name.equals(wd.name)) {
                            return true;
                        }
                    }
                }
                return false;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    private Point getDefaultDisplaySize() {
        Point p = new Point();
        ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay().getRealSize(p);
        return p;
    }

    /* JADX WARNING: Missing block: B:40:0x007e, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
                if (TextUtils.isEmpty(NOTCH_PROP) || !(HUAWEI_LAUNCHER_PACKAGE.equals(callingPackage) || HUAWEI_SYSTEMUI_PACKAGE.equals(callingPackage))) {
                    width = Math.max(width, displaySize.x);
                }
                height = Math.max(height, displaySize.y);
                if (!(width == wallpaper.width && height == wallpaper.height)) {
                    wallpaper.width = width;
                    wallpaper.height = height;
                    saveSettingsLocked(userId);
                    if (this.mCurrentUserId != userId) {
                    } else if (wallpaper.connection != null) {
                        if (wallpaper.connection.mEngine != null) {
                            try {
                                wallpaper.connection.mEngine.setDesiredSize(width, height);
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

    public int getWidthHint() throws RemoteException {
        synchronized (this.mLock) {
            WallpaperData wallpaper = (WallpaperData) this.mWallpaperMap.get(UserHandle.getCallingUserId());
            if (wallpaper != null) {
                int i = wallpaper.width;
                return i;
            }
            return 0;
        }
    }

    public int getHeightHint() throws RemoteException {
        synchronized (this.mLock) {
            WallpaperData wallpaper = (WallpaperData) this.mWallpaperMap.get(UserHandle.getCallingUserId());
            if (wallpaper != null) {
                int i = wallpaper.height;
                return i;
            }
            return 0;
        }
    }

    /* JADX WARNING: Missing block: B:36:0x0071, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setDisplayPadding(Rect padding, String callingPackage) {
        checkPermission("android.permission.SET_WALLPAPER_HINTS");
        if (isWallpaperSupported(callingPackage)) {
            synchronized (this.mLock) {
                int userId = UserHandle.getCallingUserId();
                WallpaperData wallpaper = getWallpaperSafeLocked(userId, 1);
                if (padding.left >= 0 && padding.top >= 0) {
                    if (padding.right >= 0 && padding.bottom >= 0) {
                        if (!padding.equals(wallpaper.padding)) {
                            wallpaper.padding.set(padding);
                            saveSettingsLocked(userId);
                            if (this.mCurrentUserId != userId) {
                                return;
                            } else if (wallpaper.connection != null) {
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
                throw new IllegalArgumentException("padding must be positive: " + padding);
            }
        }
    }

    public ParcelFileDescriptor getWallpaper(IWallpaperManagerCallback cb, int which, Bundle outParams, int wallpaperUserId) {
        wallpaperUserId = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), wallpaperUserId, false, true, "getWallpaper", null);
        if (which == 1 || which == 2) {
            synchronized (this.mLock) {
                SparseArray<WallpaperData> whichSet = which == 2 ? this.mLockWallpaperMap : this.mWallpaperMap;
                WallpaperData wallpaper = (WallpaperData) whichSet.get(wallpaperUserId);
                if (wallpaper == null) {
                    loadSettingsLocked(wallpaperUserId, false);
                    wallpaper = (WallpaperData) whichSet.get(wallpaperUserId);
                    if (wallpaper == null) {
                        return null;
                    }
                }
                if (outParams != null) {
                    try {
                        outParams.putInt("width", wallpaper.width);
                        outParams.putInt("height", wallpaper.height);
                    } catch (FileNotFoundException e) {
                        Slog.w(TAG, "Error getting wallpaper", e);
                        return null;
                    }
                }
                if (!(cb == null || wallpaper.callbacks.isContainIBinder(cb))) {
                    wallpaper.callbacks.register(cb);
                }
                if (wallpaper.cropFile.exists()) {
                    ParcelFileDescriptor open = ParcelFileDescriptor.open(wallpaper.cropFile, 268435456);
                    return open;
                }
                return null;
            }
        }
        throw new IllegalArgumentException("Must specify exactly one kind of wallpaper to read");
    }

    /* JADX WARNING: Missing block: B:11:0x002b, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public WallpaperInfo getWallpaperInfo(int userId) {
        userId = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, true, "getWallpaperIdForUser", null);
        synchronized (this.mLock) {
            WallpaperData wallpaper = (WallpaperData) this.mWallpaperMap.get(userId);
            if (wallpaper == null || wallpaper.connection == null) {
            } else {
                WallpaperInfo wallpaperInfo = wallpaper.connection.mInfo;
                return wallpaperInfo;
            }
        }
    }

    public int getWallpaperIdForUser(int which, int userId) {
        userId = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, true, "getWallpaperIdForUser", null);
        if (which == 1 || which == 2) {
            SparseArray<WallpaperData> map = which == 2 ? this.mLockWallpaperMap : this.mWallpaperMap;
            synchronized (this.mLock) {
                WallpaperData wallpaper = (WallpaperData) map.get(userId);
                if (wallpaper != null) {
                    int i = wallpaper.wallpaperId;
                    return i;
                }
                return -1;
            }
        }
        throw new IllegalArgumentException("Must specify exactly one kind of wallpaper");
    }

    public boolean setLockWallpaperCallback(IWallpaperManagerCallback cb) {
        checkPermission("android.permission.INTERNAL_SYSTEM_WINDOW");
        synchronized (this.mLock) {
            this.mKeyguardListener = cb;
        }
        return true;
    }

    public ParcelFileDescriptor setWallpaper(String name, String callingPackage, Rect cropHint, boolean allowBackup, Bundle extras, int which, IWallpaperManagerCallback completion, int userId) {
        if (isMdmDisableChangeWallpaper()) {
            return null;
        }
        userId = ActivityManager.handleIncomingUser(getCallingPid(), getCallingUid(), userId, false, true, "changing wallpaper", null);
        checkPermission("android.permission.SET_WALLPAPER");
        if ((which & 3) == 0) {
            String msg = "Must specify a valid wallpaper category to set";
            Slog.e(TAG, "Must specify a valid wallpaper category to set");
            throw new IllegalArgumentException("Must specify a valid wallpaper category to set");
        } else if (!isWallpaperSupported(callingPackage) || (isSetWallpaperAllowed(callingPackage) ^ 1) != 0) {
            return null;
        } else {
            ParcelFileDescriptor pfd;
            if (cropHint == null) {
                Rect rect = new Rect(0, 0, 0, 0);
            } else if (cropHint.isEmpty() || cropHint.left < 0 || cropHint.top < 0) {
                throw new IllegalArgumentException("Invalid crop rect supplied: " + cropHint);
            }
            synchronized (this.mLock) {
                if ((which & 3) == 0) {
                    if (this.mLockWallpaperMap.get(userId) == null) {
                        migrateSystemToLockWallpaperLocked(userId);
                    }
                }
                WallpaperData wallpaper = getWallpaperSafeLocked(userId, which);
                long ident = Binder.clearCallingIdentity();
                try {
                    pfd = updateWallpaperBitmapLocked(name, wallpaper, extras);
                    if (pfd != null) {
                        wallpaper.imageWallpaperPending = true;
                        wallpaper.whichPending = which;
                        wallpaper.setComplete = completion;
                        wallpaper.cropHint.set(cropHint);
                        wallpaper.allowBackup = allowBackup;
                    }
                    Binder.restoreCallingIdentity(ident);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(ident);
                }
            }
            return pfd;
        }
    }

    private void migrateSystemToLockWallpaperLocked(int userId) {
        WallpaperData sysWP = (WallpaperData) this.mWallpaperMap.get(userId);
        if (sysWP != null) {
            WallpaperData lockWP = new WallpaperData(userId, WALLPAPER_LOCK_ORIG, WALLPAPER_LOCK_CROP);
            lockWP.wallpaperId = sysWP.wallpaperId;
            lockWP.cropHint.set(sysWP.cropHint);
            lockWP.width = sysWP.width;
            lockWP.height = sysWP.height;
            lockWP.allowBackup = sysWP.allowBackup;
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

    ParcelFileDescriptor updateWallpaperBitmapLocked(String name, WallpaperData wallpaper, Bundle extras) {
        if (name == null) {
            name = "";
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
        if (!isMdmDisableChangeWallpaper()) {
            userId = ActivityManager.handleIncomingUser(getCallingPid(), getCallingUid(), userId, false, true, "changing live wallpaper", null);
            checkPermission("android.permission.SET_WALLPAPER_COMPONENT");
            synchronized (this.mLock) {
                WallpaperData wallpaper = (WallpaperData) this.mWallpaperMap.get(userId);
                if (wallpaper == null) {
                    throw new IllegalStateException("Wallpaper not yet initialized for user " + userId);
                }
                long ident = Binder.clearCallingIdentity();
                try {
                    wallpaper.imageWallpaperPending = false;
                    handleWallpaperObserverEvent(wallpaper);
                    if (bindWallpaperComponentLocked(name, false, true, wallpaper, null)) {
                        wallpaper.wallpaperId = makeWallpaperIdLocked();
                        notifyCallbacksLocked(wallpaper);
                    }
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }
    }

    boolean bindWallpaperComponentLocked(ComponentName componentName, boolean force, boolean fromUser, WallpaperData wallpaper, IRemoteCallback reply) {
        String msg;
        Slog.v(TAG, "bindWallpaperComponentLocked: componentName=" + componentName);
        if (!(force || wallpaper.connection == null)) {
            if (wallpaper.wallpaperComponent == null) {
                if (componentName == null) {
                    return true;
                }
            } else if (wallpaper.wallpaperComponent.equals(componentName)) {
                return true;
            }
        }
        if (componentName == null) {
            try {
                componentName = this.mDefaultWallpaperComponent;
                if (componentName == null) {
                    componentName = this.mImageWallpaper;
                    Slog.v(TAG, "No default component; using image wallpaper");
                }
            } catch (XmlPullParserException e) {
                if (fromUser) {
                    throw new IllegalArgumentException(e);
                }
                Slog.w(TAG, e);
                return false;
            } catch (IOException e2) {
                if (fromUser) {
                    throw new IllegalArgumentException(e2);
                }
                Slog.w(TAG, e2);
                return false;
            } catch (RemoteException e3) {
                msg = "Remote exception for " + componentName + "\n" + e3;
                if (fromUser) {
                    throw new IllegalArgumentException(msg);
                }
                Slog.w(TAG, msg);
                return false;
            }
        }
        int serviceUserId = wallpaper.userId;
        ServiceInfo si = this.mIPackageManager.getServiceInfo(componentName, 4224, serviceUserId);
        if (si == null) {
            Slog.w(TAG, "Attempted wallpaper " + componentName + " is unavailable");
            return false;
        } else if ("android.permission.BIND_WALLPAPER".equals(si.permission)) {
            WallpaperInfo wallpaperInfo = null;
            Intent intent = new Intent("android.service.wallpaper.WallpaperService");
            if (componentName != null) {
                if ((componentName.equals(this.mImageWallpaper) ^ 1) != 0) {
                    List<ResolveInfo> ris = this.mIPackageManager.queryIntentServices(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), 128, serviceUserId).getList();
                    for (int i = 0; i < ris.size(); i++) {
                        ServiceInfo rsi = ((ResolveInfo) ris.get(i)).serviceInfo;
                        if (rsi.name.equals(si.name) && rsi.packageName.equals(si.packageName)) {
                            WallpaperInfo wallpaperInfo2 = new WallpaperInfo(this.mContext, (ResolveInfo) ris.get(i));
                            break;
                        }
                    }
                    if (wallpaperInfo == null) {
                        msg = "Selected service is not a wallpaper: " + componentName;
                        if (fromUser) {
                            throw new SecurityException(msg);
                        }
                        Slog.w(TAG, msg);
                        return false;
                    }
                }
            }
            WallpaperConnection newConn = new WallpaperConnection(wallpaperInfo, wallpaper);
            intent.setComponent(componentName);
            intent.putExtra("android.intent.extra.client_label", 17041206);
            intent.putExtra("android.intent.extra.client_intent", PendingIntent.getActivityAsUser(this.mContext, 0, Intent.createChooser(new Intent("android.intent.action.SET_WALLPAPER"), this.mContext.getText(17039737)), 0, null, new UserHandle(serviceUserId)));
            if (this.mContext.bindServiceAsUser(intent, newConn, 570425345, new UserHandle(serviceUserId))) {
                reportWallpaper(componentName);
                if (wallpaper.userId == this.mCurrentUserId && this.mLastWallpaper != null) {
                    detachWallpaperLocked(this.mLastWallpaper);
                }
                wallpaper.wallpaperComponent = componentName;
                wallpaper.connection = newConn;
                newConn.mReply = reply;
                try {
                    if (wallpaper.userId == this.mCurrentUserId) {
                        this.mIWindowManager.addWindowToken(newConn.mToken, 2013, 0);
                        this.mLastWallpaper = wallpaper;
                    }
                } catch (RemoteException e4) {
                }
                return true;
            }
            msg = "Unable to bind service: " + componentName;
            if (fromUser) {
                throw new IllegalArgumentException(msg);
            }
            Slog.w(TAG, msg);
            return false;
        } else {
            msg = "Selected service does not require android.permission.BIND_WALLPAPER: " + componentName;
            if (fromUser) {
                throw new SecurityException(msg);
            }
            Slog.w(TAG, msg);
            return false;
        }
    }

    void detachWallpaperLocked(WallpaperData wallpaper) {
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

    void clearWallpaperComponentLocked(WallpaperData wallpaper) {
        wallpaper.wallpaperComponent = null;
        detachWallpaperLocked(wallpaper);
    }

    void attachServiceLocked(WallpaperConnection conn, WallpaperData wallpaper) {
        try {
            conn.mService.attach(conn, conn.mToken, 2013, false, wallpaper.width, wallpaper.height, wallpaper.padding);
        } catch (RemoteException e) {
            Slog.w(TAG, "Failed attaching wallpaper; clearing", e);
            if (!wallpaper.wallpaperUpdating) {
                bindWallpaperComponentLocked(null, false, false, wallpaper, null);
            }
        }
    }

    private void notifyCallbacksLocked(WallpaperData wallpaper) {
        synchronized (wallpaper.callbacks) {
            int n = wallpaper.callbacks.beginBroadcast();
            for (int i = 0; i < n; i++) {
                try {
                    ((IWallpaperManagerCallback) wallpaper.callbacks.getBroadcastItem(i)).onWallpaperChanged();
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
        return ((UserManager) this.mContext.getSystemService("user")).hasUserRestriction("no_set_wallpaper") ^ 1;
    }

    public boolean isWallpaperBackupEligible(int which, int userId) {
        if (Binder.getCallingUid() != 1000) {
            throw new SecurityException("Only the system may call isWallpaperBackupEligible");
        }
        WallpaperData wallpaper;
        if (which == 2) {
            wallpaper = (WallpaperData) this.mLockWallpaperMap.get(userId);
        } else {
            wallpaper = (WallpaperData) this.mWallpaperMap.get(userId);
        }
        return wallpaper != null ? wallpaper.allowBackup : false;
    }

    private static JournaledFile makeJournaledFile(int userId) {
        String base = new File(getWallpaperDir(userId), WALLPAPER_INFO).getAbsolutePath();
        return new JournaledFile(new File(base), new File(base + ".tmp"));
    }

    private void saveSettingsLocked(int userId) {
        JournaledFile journal = makeJournaledFile(userId);
        BufferedOutputStream stream = null;
        try {
            XmlSerializer out = new FastXmlSerializer();
            FileOutputStream fstream = new FileOutputStream(journal.chooseForWrite(), false);
            try {
                BufferedOutputStream stream2 = new BufferedOutputStream(fstream);
                FileOutputStream fileOutputStream;
                try {
                    out.setOutput(stream2, StandardCharsets.UTF_8.name());
                    out.startDocument(null, Boolean.valueOf(true));
                    WallpaperData wallpaper = (WallpaperData) this.mWallpaperMap.get(userId);
                    if (wallpaper != null) {
                        writeWallpaperAttributes(out, "wp", wallpaper);
                    }
                    wallpaper = (WallpaperData) this.mLockWallpaperMap.get(userId);
                    if (wallpaper != null) {
                        writeWallpaperAttributes(out, "kwp", wallpaper);
                    }
                    out.endDocument();
                    stream2.flush();
                    FileUtils.sync(fstream);
                    stream2.close();
                    journal.commit();
                    fileOutputStream = fstream;
                } catch (IOException e) {
                    stream = stream2;
                    fileOutputStream = fstream;
                    IoUtils.closeQuietly(stream);
                    journal.rollback();
                }
            } catch (IOException e2) {
                IoUtils.closeQuietly(stream);
                journal.rollback();
            }
        } catch (IOException e3) {
            IoUtils.closeQuietly(stream);
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
        out.attribute(null, "name", wallpaper.name);
        if (!(wallpaper.wallpaperComponent == null || (wallpaper.wallpaperComponent.equals(this.mImageWallpaper) ^ 1) == 0)) {
            out.attribute(null, "component", wallpaper.wallpaperComponent.flattenToShortString());
        }
        if (wallpaper.allowBackup) {
            out.attribute(null, "backup", "true");
        }
        out.endTag(null, tag);
    }

    private void migrateFromOld() {
        File oldWallpaper = new File("/data/data/com.android.settings/files/wallpaper");
        File oldInfo = new File("/data/system/wallpaper_info.xml");
        if (oldWallpaper.exists()) {
            oldWallpaper.renameTo(new File(getWallpaperDir(0), WALLPAPER));
        }
        if (oldInfo.exists()) {
            oldInfo.renameTo(new File(getWallpaperDir(0), WALLPAPER_INFO));
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
        WallpaperData wallpaper = (WallpaperData) whichSet.get(userId);
        if (wallpaper != null) {
            return wallpaper;
        }
        loadSettingsLocked(userId, false);
        wallpaper = (WallpaperData) whichSet.get(userId);
        if (wallpaper != null) {
            return wallpaper;
        }
        if (which == 2) {
            wallpaper = new WallpaperData(userId, WALLPAPER_LOCK_ORIG, WALLPAPER_LOCK_CROP);
            this.mLockWallpaperMap.put(userId, wallpaper);
            ensureSaneWallpaperData(wallpaper);
            return wallpaper;
        }
        Slog.wtf(TAG, "Didn't find wallpaper in non-lock case!");
        wallpaper = new WallpaperData(userId, WALLPAPER, WALLPAPER_CROP);
        this.mWallpaperMap.put(userId, wallpaper);
        ensureSaneWallpaperData(wallpaper);
        return wallpaper;
    }

    /* JADX WARNING: Removed duplicated region for block: B:58:0x02a0  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00f8  */
    /* JADX WARNING: Removed duplicated region for block: B:74:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0159  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00f8  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x02a0  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0159  */
    /* JADX WARNING: Removed duplicated region for block: B:74:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x02a0  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00f8  */
    /* JADX WARNING: Removed duplicated region for block: B:74:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0159  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00f8  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x02a0  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0159  */
    /* JADX WARNING: Removed duplicated region for block: B:74:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x02a0  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00f8  */
    /* JADX WARNING: Removed duplicated region for block: B:74:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0159  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00f8  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x02a0  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0159  */
    /* JADX WARNING: Removed duplicated region for block: B:74:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x02a0  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00f8  */
    /* JADX WARNING: Removed duplicated region for block: B:74:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0159  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00f8  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x02a0  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0159  */
    /* JADX WARNING: Removed duplicated region for block: B:74:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x02a0  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00f8  */
    /* JADX WARNING: Removed duplicated region for block: B:74:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0159  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00f8  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x02a0  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0159  */
    /* JADX WARNING: Removed duplicated region for block: B:74:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x02a0  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00f8  */
    /* JADX WARNING: Removed duplicated region for block: B:74:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0159  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00f8  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x02a0  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0159  */
    /* JADX WARNING: Removed duplicated region for block: B:74:? A:{SYNTHETIC, RETURN} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void loadSettingsLocked(int userId, boolean keepDimensionHints) {
        WallpaperData lockWallpaper;
        NullPointerException e;
        Object stream;
        NumberFormatException e2;
        XmlPullParserException e3;
        IOException e4;
        IndexOutOfBoundsException e5;
        AutoCloseable stream2 = null;
        File file = makeJournaledFile(userId).chooseForRead();
        if (!file.exists()) {
            migrateFromOld();
        }
        WallpaperData wallpaper = (WallpaperData) this.mWallpaperMap.get(userId);
        if (wallpaper == null) {
            WallpaperData wallpaperData = new WallpaperData(userId, WALLPAPER, WALLPAPER_CROP);
            wallpaperData.allowBackup = true;
            this.mWallpaperMap.put(userId, wallpaperData);
            if (!wallpaperData.cropExists()) {
                if (wallpaperData.sourceExists()) {
                    generateCrop(wallpaperData);
                } else {
                    Slog.i(TAG, "No static wallpaper imagery; defaults will be shown");
                }
            }
        }
        boolean success = false;
        try {
            InputStream fileInputStream = new FileInputStream(file);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(fileInputStream, StandardCharsets.UTF_8.name());
                int type;
                do {
                    type = parser.next();
                    if (type == 2) {
                        String tag = parser.getName();
                        if ("wp".equals(tag)) {
                            ComponentName unflattenFromString;
                            parseWallpaperAttributes(parser, wallpaper, keepDimensionHints);
                            String comp = parser.getAttributeValue(null, "component");
                            if (comp != null) {
                                unflattenFromString = ComponentName.unflattenFromString(comp);
                            } else {
                                unflattenFromString = null;
                            }
                            wallpaper.nextWallpaperComponent = unflattenFromString;
                            if (wallpaper.nextWallpaperComponent == null || "android".equals(wallpaper.nextWallpaperComponent.getPackageName())) {
                                wallpaper.nextWallpaperComponent = this.mImageWallpaper;
                            }
                        } else if ("kwp".equals(tag)) {
                            lockWallpaper = (WallpaperData) this.mLockWallpaperMap.get(userId);
                            if (lockWallpaper == null) {
                                lockWallpaper = new WallpaperData(userId, WALLPAPER_LOCK_ORIG, WALLPAPER_LOCK_CROP);
                                this.mLockWallpaperMap.put(userId, lockWallpaper);
                            }
                            parseWallpaperAttributes(parser, lockWallpaper, false);
                        }
                    }
                } while (type != 1);
                success = true;
                stream2 = fileInputStream;
            } catch (FileNotFoundException e6) {
                stream2 = fileInputStream;
                Slog.w(TAG, "no current wallpaper -- first boot?");
                IoUtils.closeQuietly(stream2);
                this.mSuccess = success;
                if (success) {
                }
                ensureSaneWallpaperData(wallpaper);
                lockWallpaper = (WallpaperData) this.mLockWallpaperMap.get(userId);
                if (lockWallpaper != null) {
                }
            } catch (NullPointerException e7) {
                e = e7;
                stream2 = fileInputStream;
                Slog.w(TAG, "failed parsing " + file + " " + e);
                IoUtils.closeQuietly(stream2);
                this.mSuccess = success;
                if (success) {
                }
                ensureSaneWallpaperData(wallpaper);
                lockWallpaper = (WallpaperData) this.mLockWallpaperMap.get(userId);
                if (lockWallpaper != null) {
                }
            } catch (NumberFormatException e8) {
                e2 = e8;
                stream2 = fileInputStream;
                Slog.w(TAG, "failed parsing " + file + " " + e2);
                IoUtils.closeQuietly(stream2);
                this.mSuccess = success;
                if (success) {
                }
                ensureSaneWallpaperData(wallpaper);
                lockWallpaper = (WallpaperData) this.mLockWallpaperMap.get(userId);
                if (lockWallpaper != null) {
                }
            } catch (XmlPullParserException e9) {
                e3 = e9;
                stream2 = fileInputStream;
                Slog.w(TAG, "failed parsing " + file + " " + e3);
                IoUtils.closeQuietly(stream2);
                this.mSuccess = success;
                if (success) {
                }
                ensureSaneWallpaperData(wallpaper);
                lockWallpaper = (WallpaperData) this.mLockWallpaperMap.get(userId);
                if (lockWallpaper != null) {
                }
            } catch (IOException e10) {
                e4 = e10;
                stream2 = fileInputStream;
                Slog.w(TAG, "failed parsing " + file + " " + e4);
                IoUtils.closeQuietly(stream2);
                this.mSuccess = success;
                if (success) {
                }
                ensureSaneWallpaperData(wallpaper);
                lockWallpaper = (WallpaperData) this.mLockWallpaperMap.get(userId);
                if (lockWallpaper != null) {
                }
            } catch (IndexOutOfBoundsException e11) {
                e5 = e11;
                stream2 = fileInputStream;
                Slog.w(TAG, "failed parsing " + file + " " + e5);
                IoUtils.closeQuietly(stream2);
                this.mSuccess = success;
                if (success) {
                }
                ensureSaneWallpaperData(wallpaper);
                lockWallpaper = (WallpaperData) this.mLockWallpaperMap.get(userId);
                if (lockWallpaper != null) {
                }
            }
        } catch (FileNotFoundException e12) {
            Slog.w(TAG, "no current wallpaper -- first boot?");
            IoUtils.closeQuietly(stream2);
            this.mSuccess = success;
            if (success) {
            }
            ensureSaneWallpaperData(wallpaper);
            lockWallpaper = (WallpaperData) this.mLockWallpaperMap.get(userId);
            if (lockWallpaper != null) {
            }
        } catch (NullPointerException e13) {
            e = e13;
            Slog.w(TAG, "failed parsing " + file + " " + e);
            IoUtils.closeQuietly(stream2);
            this.mSuccess = success;
            if (success) {
            }
            ensureSaneWallpaperData(wallpaper);
            lockWallpaper = (WallpaperData) this.mLockWallpaperMap.get(userId);
            if (lockWallpaper != null) {
            }
        } catch (NumberFormatException e14) {
            e2 = e14;
            Slog.w(TAG, "failed parsing " + file + " " + e2);
            IoUtils.closeQuietly(stream2);
            this.mSuccess = success;
            if (success) {
            }
            ensureSaneWallpaperData(wallpaper);
            lockWallpaper = (WallpaperData) this.mLockWallpaperMap.get(userId);
            if (lockWallpaper != null) {
            }
        } catch (XmlPullParserException e15) {
            e3 = e15;
            Slog.w(TAG, "failed parsing " + file + " " + e3);
            IoUtils.closeQuietly(stream2);
            this.mSuccess = success;
            if (success) {
            }
            ensureSaneWallpaperData(wallpaper);
            lockWallpaper = (WallpaperData) this.mLockWallpaperMap.get(userId);
            if (lockWallpaper != null) {
            }
        } catch (IOException e16) {
            e4 = e16;
            Slog.w(TAG, "failed parsing " + file + " " + e4);
            IoUtils.closeQuietly(stream2);
            this.mSuccess = success;
            if (success) {
            }
            ensureSaneWallpaperData(wallpaper);
            lockWallpaper = (WallpaperData) this.mLockWallpaperMap.get(userId);
            if (lockWallpaper != null) {
            }
        } catch (IndexOutOfBoundsException e17) {
            e5 = e17;
            Slog.w(TAG, "failed parsing " + file + " " + e5);
            IoUtils.closeQuietly(stream2);
            this.mSuccess = success;
            if (success) {
            }
            ensureSaneWallpaperData(wallpaper);
            lockWallpaper = (WallpaperData) this.mLockWallpaperMap.get(userId);
            if (lockWallpaper != null) {
            }
        }
        IoUtils.closeQuietly(stream2);
        this.mSuccess = success;
        if (success) {
            wallpaper.width = -1;
            wallpaper.height = -1;
            wallpaper.cropHint.set(0, 0, 0, 0);
            wallpaper.padding.set(0, 0, 0, 0);
            wallpaper.name = "";
            this.mLockWallpaperMap.remove(userId);
        } else if (wallpaper.wallpaperId <= 0) {
            wallpaper.wallpaperId = makeWallpaperIdLocked();
        }
        ensureSaneWallpaperData(wallpaper);
        lockWallpaper = (WallpaperData) this.mLockWallpaperMap.get(userId);
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
        wallpaper.name = parser.getAttributeValue(null, "name");
        wallpaper.allowBackup = "true".equals(parser.getAttributeValue(null, "backup"));
    }

    private int getMaximumSizeDimension() {
        return ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay().getMaximumSizeDimension();
    }

    public void settingsRestored() {
        if (Binder.getCallingUid() != 1000) {
            throw new RuntimeException("settingsRestored() can only be called from the system process");
        }
        WallpaperData wallpaper;
        boolean success;
        synchronized (this.mLock) {
            loadSettingsLocked(0, false);
            wallpaper = (WallpaperData) this.mWallpaperMap.get(0);
            wallpaper.wallpaperId = makeWallpaperIdLocked();
            wallpaper.allowBackup = true;
            if (wallpaper.nextWallpaperComponent == null || (wallpaper.nextWallpaperComponent.equals(this.mImageWallpaper) ^ 1) == 0) {
                if ("".equals(wallpaper.name)) {
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
            wallpaper.name = "";
            getWallpaperDir(0).delete();
        }
        synchronized (this.mLock) {
            saveSettingsLocked(0);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:46:0x0185  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x018a  */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x0210  */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x0215  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x01dd  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x01e2  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0185  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x018a  */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x0210  */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x0215  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x01dd  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x01e2  */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x0226  */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x022b  */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x0226  */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x022b  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean restoreNamedResourceLocked(WallpaperData wallpaper) {
        Object fos;
        Throwable th;
        IOException e;
        Object cos;
        if (wallpaper.name.length() > 4 && "res:".equals(wallpaper.name.substring(0, 4))) {
            String resName = wallpaper.name.substring(4);
            String pkg = null;
            int colon = resName.indexOf(58);
            if (colon > 0) {
                pkg = resName.substring(0, colon);
            }
            String ident = null;
            int slash = resName.lastIndexOf(47);
            if (slash > 0) {
                ident = resName.substring(slash + 1);
            }
            String type = null;
            if (colon > 0 && slash > 0 && slash - colon > 1) {
                type = resName.substring(colon + 1, slash);
            }
            if (!(pkg == null || ident == null || type == null)) {
                int resId = -1;
                AutoCloseable autoCloseable = null;
                AutoCloseable fos2 = null;
                AutoCloseable cos2 = null;
                try {
                    Resources r = this.mContext.createPackageContext(pkg, 4).getResources();
                    resId = r.getIdentifier(resName, null, null);
                    if (resId == 0) {
                        Slog.e(TAG, "couldn't resolve identifier pkg=" + pkg + " type=" + type + " ident=" + ident);
                        IoUtils.closeQuietly(null);
                        IoUtils.closeQuietly(null);
                        IoUtils.closeQuietly(null);
                        return false;
                    }
                    FileOutputStream cos3;
                    autoCloseable = r.openRawResource(resId);
                    if (wallpaper.wallpaperFile.exists()) {
                        wallpaper.wallpaperFile.delete();
                        wallpaper.cropFile.delete();
                    }
                    FileOutputStream fos3 = new FileOutputStream(wallpaper.wallpaperFile);
                    try {
                        cos3 = new FileOutputStream(wallpaper.cropFile);
                    } catch (NameNotFoundException e2) {
                        fos = fos3;
                        try {
                            Slog.e(TAG, "Package name " + pkg + " not found");
                            IoUtils.closeQuietly(autoCloseable);
                            if (fos2 != null) {
                            }
                            if (cos2 != null) {
                            }
                            IoUtils.closeQuietly(fos2);
                            IoUtils.closeQuietly(cos2);
                            return false;
                        } catch (Throwable th2) {
                            th = th2;
                            IoUtils.closeQuietly(autoCloseable);
                            if (fos2 != null) {
                            }
                            if (cos2 != null) {
                            }
                            IoUtils.closeQuietly(fos2);
                            IoUtils.closeQuietly(cos2);
                            throw th;
                        }
                    } catch (NotFoundException e3) {
                        fos = fos3;
                        Slog.e(TAG, "Resource not found: " + resId);
                        IoUtils.closeQuietly(autoCloseable);
                        if (fos2 != null) {
                        }
                        if (cos2 != null) {
                        }
                        IoUtils.closeQuietly(fos2);
                        IoUtils.closeQuietly(cos2);
                        return false;
                    } catch (IOException e4) {
                        e = e4;
                        fos = fos3;
                        Slog.e(TAG, "IOException while restoring wallpaper ", e);
                        IoUtils.closeQuietly(autoCloseable);
                        if (fos2 != null) {
                        }
                        if (cos2 != null) {
                        }
                        IoUtils.closeQuietly(fos2);
                        IoUtils.closeQuietly(cos2);
                        return false;
                    } catch (Throwable th3) {
                        th = th3;
                        fos = fos3;
                        IoUtils.closeQuietly(autoCloseable);
                        if (fos2 != null) {
                        }
                        if (cos2 != null) {
                        }
                        IoUtils.closeQuietly(fos2);
                        IoUtils.closeQuietly(cos2);
                        throw th;
                    }
                    try {
                        byte[] buffer = new byte[32768];
                        while (true) {
                            int amt = autoCloseable.read(buffer);
                            if (amt <= 0) {
                                break;
                            }
                            fos3.write(buffer, 0, amt);
                            cos3.write(buffer, 0, amt);
                        }
                        Slog.v(TAG, "Restored wallpaper: " + resName);
                        IoUtils.closeQuietly(autoCloseable);
                        if (fos3 != null) {
                            FileUtils.sync(fos3);
                        }
                        if (cos3 != null) {
                            FileUtils.sync(cos3);
                        }
                        IoUtils.closeQuietly(fos3);
                        IoUtils.closeQuietly(cos3);
                        return true;
                    } catch (NameNotFoundException e5) {
                        cos2 = cos3;
                        fos2 = fos3;
                        Slog.e(TAG, "Package name " + pkg + " not found");
                        IoUtils.closeQuietly(autoCloseable);
                        if (fos2 != null) {
                            FileUtils.sync(fos2);
                        }
                        if (cos2 != null) {
                            FileUtils.sync(cos2);
                        }
                        IoUtils.closeQuietly(fos2);
                        IoUtils.closeQuietly(cos2);
                        return false;
                    } catch (NotFoundException e6) {
                        cos = cos3;
                        fos = fos3;
                        Slog.e(TAG, "Resource not found: " + resId);
                        IoUtils.closeQuietly(autoCloseable);
                        if (fos2 != null) {
                            FileUtils.sync(fos2);
                        }
                        if (cos2 != null) {
                            FileUtils.sync(cos2);
                        }
                        IoUtils.closeQuietly(fos2);
                        IoUtils.closeQuietly(cos2);
                        return false;
                    } catch (IOException e7) {
                        e = e7;
                        cos = cos3;
                        fos = fos3;
                        Slog.e(TAG, "IOException while restoring wallpaper ", e);
                        IoUtils.closeQuietly(autoCloseable);
                        if (fos2 != null) {
                            FileUtils.sync(fos2);
                        }
                        if (cos2 != null) {
                            FileUtils.sync(cos2);
                        }
                        IoUtils.closeQuietly(fos2);
                        IoUtils.closeQuietly(cos2);
                        return false;
                    } catch (Throwable th4) {
                        th = th4;
                        cos = cos3;
                        fos = fos3;
                        IoUtils.closeQuietly(autoCloseable);
                        if (fos2 != null) {
                            FileUtils.sync(fos2);
                        }
                        if (cos2 != null) {
                            FileUtils.sync(cos2);
                        }
                        IoUtils.closeQuietly(fos2);
                        IoUtils.closeQuietly(cos2);
                        throw th;
                    }
                } catch (NameNotFoundException e8) {
                    Slog.e(TAG, "Package name " + pkg + " not found");
                    IoUtils.closeQuietly(autoCloseable);
                    if (fos2 != null) {
                    }
                    if (cos2 != null) {
                    }
                    IoUtils.closeQuietly(fos2);
                    IoUtils.closeQuietly(cos2);
                    return false;
                } catch (NotFoundException e9) {
                    Slog.e(TAG, "Resource not found: " + resId);
                    IoUtils.closeQuietly(autoCloseable);
                    if (fos2 != null) {
                    }
                    if (cos2 != null) {
                    }
                    IoUtils.closeQuietly(fos2);
                    IoUtils.closeQuietly(cos2);
                    return false;
                } catch (IOException e10) {
                    e = e10;
                    Slog.e(TAG, "IOException while restoring wallpaper ", e);
                    IoUtils.closeQuietly(autoCloseable);
                    if (fos2 != null) {
                    }
                    if (cos2 != null) {
                    }
                    IoUtils.closeQuietly(fos2);
                    IoUtils.closeQuietly(cos2);
                    return false;
                }
            }
        }
        return false;
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, pw)) {
            synchronized (this.mLock) {
                int i;
                WallpaperData wallpaper;
                pw.println("System wallpaper state:");
                for (i = 0; i < this.mWallpaperMap.size(); i++) {
                    wallpaper = (WallpaperData) this.mWallpaperMap.valueAt(i);
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
                for (i = 0; i < this.mLockWallpaperMap.size(); i++) {
                    wallpaper = (WallpaperData) this.mLockWallpaperMap.valueAt(i);
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
                }
            }
        }
    }

    public void handleWallpaperObserverEvent(WallpaperData wallpaper) {
    }

    public ParcelFileDescriptor getBlurWallpaper(IWallpaperManagerCallback cb) {
        return null;
    }

    protected SparseArray<WallpaperData> getWallpaperMap() {
        return this.mWallpaperMap;
    }

    protected Context getContext() {
        return this.mContext;
    }

    protected Object getLock() {
        return this.mLock;
    }

    protected int getCurrentUserId() {
        return this.mCurrentUserId;
    }

    protected boolean getHwWallpaperWidth(WallpaperData wallpaper, boolean success) {
        return false;
    }

    protected void updateWallpaperOffsets(WallpaperData wallpaper) {
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
                LayoutParams windowParams = toast.getWindowParams();
                windowParams.privateFlags |= 16;
                toast.show();
            }
        }, 300);
        return true;
    }

    public void reportWallpaper(ComponentName componentName) {
    }
}
