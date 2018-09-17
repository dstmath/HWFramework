package com.android.server.wallpaper;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.IUserSwitchObserver;
import android.app.IWallpaperManager.Stub;
import android.app.IWallpaperManagerCallback;
import android.app.PendingIntent;
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
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.FileUtils;
import android.os.IBinder;
import android.os.IRemoteCallback;
import android.os.ParcelFileDescriptor;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SELinux;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
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
import com.android.internal.content.PackageMonitor;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.JournaledFile;
import com.android.server.EventLogTags;
import com.android.server.SystemService;
import com.android.server.am.ProcessList;
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
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class WallpaperManagerService extends Stub {
    static final boolean DEBUG = false;
    static final int MAX_WALLPAPER_COMPONENT_LOG_LENGTH = 128;
    static final long MIN_WALLPAPER_CRASH_TIME = 10000;
    static final String TAG = "WallpaperManagerService";
    static final String WALLPAPER = "wallpaper_orig";
    static final String WALLPAPER_CROP = "wallpaper";
    static final String WALLPAPER_INFO = "wallpaper_info.xml";
    static final String WALLPAPER_LOCK_CROP = "wallpaper_lock";
    static final String WALLPAPER_LOCK_ORIG = "wallpaper_lock_orig";
    static final String[] sPerUserFiles = null;
    final AppOpsManager mAppOpsManager;
    final Context mContext;
    int mCurrentUserId;
    final IPackageManager mIPackageManager;
    final IWindowManager mIWindowManager;
    final ComponentName mImageWallpaper;
    IWallpaperManagerCallback mKeyguardListener;
    WallpaperData mLastWallpaper;
    final Object mLock;
    final SparseArray<WallpaperData> mLockWallpaperMap;
    final MyPackageMonitor mMonitor;
    boolean mSuccess;
    boolean mWaitingForUnlock;
    int mWallpaperId;
    final SparseArray<WallpaperData> mWallpaperMap;

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
            if (phase == SystemService.PHASE_ACTIVITY_MANAGER_READY) {
                this.mService.systemReady();
            } else if (phase == NetdResponseCode.InterfaceChange) {
                this.mService.switchUser(0, null);
            }
        }

        public void onUnlockUser(int userHandle) {
            this.mService.onUnlockUser(userHandle);
        }
    }

    class MyPackageMonitor extends PackageMonitor {
        MyPackageMonitor() {
        }

        public void onPackageUpdateFinished(String packageName, int uid) {
            synchronized (WallpaperManagerService.this.mLock) {
                if (WallpaperManagerService.this.mCurrentUserId != getChangingUserId()) {
                    return;
                }
                WallpaperData wallpaper = (WallpaperData) WallpaperManagerService.this.mWallpaperMap.get(WallpaperManagerService.this.mCurrentUserId);
                if (!(wallpaper == null || wallpaper.wallpaperComponent == null || !wallpaper.wallpaperComponent.getPackageName().equals(packageName))) {
                    wallpaper.wallpaperUpdating = WallpaperManagerService.DEBUG;
                    ComponentName comp = wallpaper.wallpaperComponent;
                    WallpaperManagerService.this.clearWallpaperComponentLocked(wallpaper);
                    if (!WallpaperManagerService.this.bindWallpaperComponentLocked(comp, WallpaperManagerService.DEBUG, WallpaperManagerService.DEBUG, wallpaper, null)) {
                        Slog.w(WallpaperManagerService.TAG, "Wallpaper no longer available; reverting to default");
                        WallpaperManagerService.this.clearWallpaperLocked(WallpaperManagerService.DEBUG, 1, wallpaper.userId, null);
                    }
                }
            }
        }

        public void onPackageModified(String packageName) {
            synchronized (WallpaperManagerService.this.mLock) {
                if (WallpaperManagerService.this.mCurrentUserId != getChangingUserId()) {
                    return;
                }
                WallpaperData wallpaper = (WallpaperData) WallpaperManagerService.this.mWallpaperMap.get(WallpaperManagerService.this.mCurrentUserId);
                if (wallpaper != null) {
                    if (wallpaper.wallpaperComponent == null || !wallpaper.wallpaperComponent.getPackageName().equals(packageName)) {
                        return;
                    }
                    doPackagesChangedLocked(true, wallpaper);
                }
            }
        }

        public void onPackageUpdateStarted(String packageName, int uid) {
            synchronized (WallpaperManagerService.this.mLock) {
                if (WallpaperManagerService.this.mCurrentUserId != getChangingUserId()) {
                    return;
                }
                WallpaperData wallpaper = (WallpaperData) WallpaperManagerService.this.mWallpaperMap.get(WallpaperManagerService.this.mCurrentUserId);
                if (!(wallpaper == null || wallpaper.wallpaperComponent == null || !wallpaper.wallpaperComponent.getPackageName().equals(packageName))) {
                    wallpaper.wallpaperUpdating = true;
                }
            }
        }

        public boolean onHandleForceStop(Intent intent, String[] packages, int uid, boolean doit) {
            synchronized (WallpaperManagerService.this.mLock) {
                boolean changed = WallpaperManagerService.DEBUG;
                if (WallpaperManagerService.this.mCurrentUserId != getChangingUserId()) {
                    return WallpaperManagerService.DEBUG;
                }
                WallpaperData wallpaper = (WallpaperData) WallpaperManagerService.this.mWallpaperMap.get(WallpaperManagerService.this.mCurrentUserId);
                if (wallpaper != null) {
                    changed = doPackagesChangedLocked(doit, wallpaper);
                }
                return changed;
            }
        }

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
            boolean changed = WallpaperManagerService.DEBUG;
            if (wallpaper.wallpaperComponent != null) {
                change = isPackageDisappearing(wallpaper.wallpaperComponent.getPackageName());
                if (change == 3 || change == 2) {
                    changed = true;
                    if (doit) {
                        Slog.w(WallpaperManagerService.TAG, "Wallpaper uninstalled, removing: " + wallpaper.wallpaperComponent);
                        WallpaperManagerService.this.clearWallpaperLocked(WallpaperManagerService.DEBUG, 1, wallpaper.userId, null);
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
                        WallpaperManagerService.this.clearWallpaperLocked(WallpaperManagerService.DEBUG, 1, wallpaper.userId, null);
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
        boolean mDimensionsChanged;
        IWallpaperEngine mEngine;
        final WallpaperInfo mInfo;
        boolean mPaddingChanged;
        IRemoteCallback mReply;
        IWallpaperService mService;
        final Binder mToken;
        WallpaperData mWallpaper;

        public WallpaperConnection(WallpaperInfo info, WallpaperData wallpaper) {
            this.mToken = new Binder();
            this.mDimensionsChanged = WallpaperManagerService.DEBUG;
            this.mPaddingChanged = WallpaperManagerService.DEBUG;
            this.mInfo = info;
            this.mWallpaper = wallpaper;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (WallpaperManagerService.this.mLock) {
                if (this.mWallpaper.connection == this) {
                    this.mService = IWallpaperService.Stub.asInterface(service);
                    WallpaperManagerService.this.attachServiceLocked(this, this.mWallpaper);
                    WallpaperManagerService.this.saveSettingsLocked(this.mWallpaper.userId);
                }
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            synchronized (WallpaperManagerService.this.mLock) {
                this.mService = null;
                this.mEngine = null;
                if (this.mWallpaper.connection == this) {
                    Slog.w(WallpaperManagerService.TAG, "Wallpaper service gone: " + this.mWallpaper.wallpaperComponent);
                    if (!this.mWallpaper.wallpaperUpdating && this.mWallpaper.userId == WallpaperManagerService.this.mCurrentUserId) {
                        if (this.mWallpaper.lastDiedTime == 0 || this.mWallpaper.lastDiedTime + WallpaperManagerService.MIN_WALLPAPER_CRASH_TIME <= SystemClock.uptimeMillis()) {
                            this.mWallpaper.lastDiedTime = SystemClock.uptimeMillis();
                        } else {
                            Slog.w(WallpaperManagerService.TAG, "Reverting to built-in wallpaper!");
                            WallpaperManagerService.this.clearWallpaperLocked(true, 1, this.mWallpaper.userId, null);
                        }
                        String flattened = name.flattenToString();
                        EventLog.writeEvent(EventLogTags.WP_WALLPAPER_CRASHED, flattened.substring(0, Math.min(flattened.length(), WallpaperManagerService.MAX_WALLPAPER_COMPONENT_LOG_LENGTH)));
                    }
                }
            }
        }

        public void attachEngine(IWallpaperEngine engine) {
            synchronized (WallpaperManagerService.this.mLock) {
                this.mEngine = engine;
                if (this.mDimensionsChanged) {
                    try {
                        this.mEngine.setDesiredSize(this.mWallpaper.width, this.mWallpaper.height);
                    } catch (RemoteException e) {
                        Slog.w(WallpaperManagerService.TAG, "Failed to set wallpaper dimensions", e);
                    }
                    this.mDimensionsChanged = WallpaperManagerService.DEBUG;
                }
                if (this.mPaddingChanged) {
                    try {
                        this.mEngine.setDisplayPadding(this.mWallpaper.padding);
                    } catch (RemoteException e2) {
                        Slog.w(WallpaperManagerService.TAG, "Failed to set wallpaper padding", e2);
                    }
                    this.mPaddingChanged = WallpaperManagerService.DEBUG;
                }
            }
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
        private RemoteCallbackList<IWallpaperManagerCallback> callbacks;
        WallpaperConnection connection;
        final File cropFile;
        final Rect cropHint;
        int[] currOffsets;
        int height;
        boolean imageWallpaperPending;
        long lastDiedTime;
        String name;
        int[] nextOffsets;
        ComponentName nextWallpaperComponent;
        final Rect padding;
        IWallpaperManagerCallback setComplete;
        int userId;
        ComponentName wallpaperComponent;
        final File wallpaperFile;
        int wallpaperId;
        WallpaperObserver wallpaperObserver;
        boolean wallpaperUpdating;
        int whichPending;
        int width;

        WallpaperData(int userId, String inputFileName, String cropFileName) {
            this.name = "";
            this.callbacks = new RemoteCallbackList();
            this.width = -1;
            this.height = -1;
            this.cropHint = new Rect(0, 0, 0, 0);
            this.padding = new Rect(0, 0, 0, 0);
            this.currOffsets = new int[]{-1, -1, -1, -1};
            this.nextOffsets = new int[]{-1, -1, -1, -1};
            this.userId = userId;
            File wallpaperDir = WallpaperManagerService.getWallpaperDir(userId);
            this.wallpaperFile = new File(wallpaperDir, inputFileName);
            this.cropFile = new File(wallpaperDir, cropFileName);
        }

        boolean cropExists() {
            return this.cropFile.exists();
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
        final File mWallpaperFile;
        final File mWallpaperInfoFile;
        final File mWallpaperLockFile;

        public WallpaperObserver(WallpaperData wallpaper) {
            super(WallpaperManagerService.getWallpaperDir(wallpaper.userId).getAbsolutePath(), 1672);
            this.mUserId = wallpaper.userId;
            this.mWallpaperDir = WallpaperManagerService.getWallpaperDir(wallpaper.userId);
            this.mWallpaper = wallpaper;
            this.mWallpaperFile = new File(this.mWallpaperDir, WallpaperManagerService.WALLPAPER);
            this.mWallpaperLockFile = new File(this.mWallpaperDir, WallpaperManagerService.WALLPAPER_LOCK_ORIG);
            this.mWallpaperInfoFile = new File(this.mWallpaperDir, WallpaperManagerService.WALLPAPER_INFO);
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

        public void onEvent(int event, String path) {
            if (path != null) {
                boolean moved = event == WallpaperManagerService.MAX_WALLPAPER_COMPONENT_LOG_LENGTH ? true : WallpaperManagerService.DEBUG;
                boolean z = event != 8 ? moved : true;
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
                            if (wallpaper.imageWallpaperPending) {
                            }
                        }
                        if (z) {
                            if (moved) {
                                SELinux.restorecon(changedFile);
                                WallpaperManagerService.this.loadSettingsLocked(wallpaper.userId, true);
                            }
                            WallpaperManagerService.this.generateCrop(wallpaper);
                            wallpaper.imageWallpaperPending = WallpaperManagerService.DEBUG;
                            if (wallpaper.setComplete != null) {
                                try {
                                    wallpaper.setComplete.onWallpaperChanged();
                                } catch (RemoteException e) {
                                }
                            }
                            if (sysWallpaperChanged) {
                                WallpaperManagerService.this.bindWallpaperComponentLocked(WallpaperManagerService.this.mImageWallpaper, WallpaperManagerService.DEBUG, WallpaperManagerService.DEBUG, wallpaper, null);
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wallpaper.WallpaperManagerService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wallpaper.WallpaperManagerService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wallpaper.WallpaperManagerService.<clinit>():void");
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

    private void generateCrop(WallpaperData wallpaper) {
        boolean restorecon;
        Throwable th;
        boolean success = DEBUG;
        Rect cropHint = new Rect(wallpaper.cropHint);
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(wallpaper.wallpaperFile.getAbsolutePath(), options);
        if (options.outWidth <= 0 || options.outHeight <= 0) {
            Slog.e(TAG, "Invalid wallpaper data");
            success = DEBUG;
        } else {
            boolean needCrop = DEBUG;
            if (cropHint.isEmpty()) {
                cropHint.top = 0;
                cropHint.left = 0;
                cropHint.right = options.outWidth;
                cropHint.bottom = options.outHeight;
            } else {
                cropHint.offset(cropHint.right > options.outWidth ? options.outWidth - cropHint.right : 0, cropHint.bottom > options.outHeight ? options.outHeight - cropHint.bottom : 0);
                if (options.outHeight >= cropHint.height()) {
                    if (options.outWidth >= cropHint.width()) {
                        needCrop = true;
                    } else {
                        needCrop = DEBUG;
                    }
                } else {
                    needCrop = DEBUG;
                }
            }
            boolean needScale = wallpaper.height != cropHint.height() ? true : DEBUG;
            if (needCrop || needScale) {
                AutoCloseable autoCloseable = null;
                BufferedOutputStream bos = null;
                try {
                    Options scaler;
                    BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(wallpaper.wallpaperFile.getAbsolutePath(), DEBUG);
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
                        FileOutputStream f = new FileOutputStream(wallpaper.cropFile);
                        Object f2;
                        try {
                            BufferedOutputStream bos2 = new BufferedOutputStream(f, DumpState.DUMP_VERSION);
                            try {
                                finalCrop.compress(CompressFormat.JPEG, 100, bos2);
                                bos2.flush();
                                success = true;
                                bos = bos2;
                                f2 = f;
                            } catch (Exception e) {
                                bos = bos2;
                                f2 = f;
                                IoUtils.closeQuietly(bos);
                                IoUtils.closeQuietly(autoCloseable);
                                if (!success) {
                                    Slog.e(TAG, "Unable to apply new wallpaper");
                                    wallpaper.cropFile.delete();
                                }
                                if (!wallpaper.cropFile.exists()) {
                                    restorecon = SELinux.restorecon(wallpaper.cropFile.getAbsoluteFile());
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                bos = bos2;
                                f2 = f;
                                IoUtils.closeQuietly(bos);
                                IoUtils.closeQuietly(autoCloseable);
                                throw th;
                            }
                        } catch (Exception e2) {
                            f2 = f;
                            IoUtils.closeQuietly(bos);
                            IoUtils.closeQuietly(autoCloseable);
                            if (success) {
                                Slog.e(TAG, "Unable to apply new wallpaper");
                                wallpaper.cropFile.delete();
                            }
                            if (!wallpaper.cropFile.exists()) {
                                restorecon = SELinux.restorecon(wallpaper.cropFile.getAbsoluteFile());
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            f2 = f;
                            IoUtils.closeQuietly(bos);
                            IoUtils.closeQuietly(autoCloseable);
                            throw th;
                        }
                    }
                    IoUtils.closeQuietly(bos);
                    IoUtils.closeQuietly(autoCloseable);
                } catch (Exception e3) {
                    IoUtils.closeQuietly(bos);
                    IoUtils.closeQuietly(autoCloseable);
                    if (success) {
                        Slog.e(TAG, "Unable to apply new wallpaper");
                        wallpaper.cropFile.delete();
                    }
                    if (!wallpaper.cropFile.exists()) {
                        restorecon = SELinux.restorecon(wallpaper.cropFile.getAbsoluteFile());
                    }
                } catch (Throwable th4) {
                    th = th4;
                    IoUtils.closeQuietly(bos);
                    IoUtils.closeQuietly(autoCloseable);
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
        if (!wallpaper.cropFile.exists()) {
            restorecon = SELinux.restorecon(wallpaper.cropFile.getAbsoluteFile());
        }
    }

    int makeWallpaperIdLocked() {
        do {
            this.mWallpaperId++;
        } while (this.mWallpaperId == 0);
        return this.mWallpaperId;
    }

    public WallpaperManagerService(Context context) {
        this.mLock = new Object();
        this.mWallpaperMap = new SparseArray();
        this.mLockWallpaperMap = new SparseArray();
        this.mSuccess = DEBUG;
        this.mContext = context;
        this.mImageWallpaper = ComponentName.unflattenFromString(context.getResources().getString(17039422));
        this.mIWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
        this.mIPackageManager = AppGlobals.getPackageManager();
        this.mAppOpsManager = (AppOpsManager) this.mContext.getSystemService("appops");
        this.mMonitor = new MyPackageMonitor();
        this.mMonitor.register(context, null, UserHandle.ALL, true);
        getWallpaperDir(0).mkdirs();
        loadSettingsLocked(0, DEBUG);
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
                clearWallpaperLocked(DEBUG, 1, 0, null);
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
        try {
            ActivityManagerNative.getDefault().registerUserSwitchObserver(new IUserSwitchObserver.Stub() {
                public void onUserSwitching(int newUserId, IRemoteCallback reply) {
                    WallpaperManagerService.this.switchUser(newUserId, reply);
                }

                public void onUserSwitchComplete(int newUserId) throws RemoteException {
                }

                public void onForegroundProfileSwitch(int newProfileId) {
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        if (Binder.getCallingUid() != ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE) {
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

    void onUnlockUser(int userId) {
        synchronized (this.mLock) {
            if (this.mCurrentUserId == userId && this.mWaitingForUnlock) {
                switchUser(userId, null);
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
            this.mWaitingForUnlock = DEBUG;
            ComponentName cname = wallpaper.wallpaperComponent != null ? wallpaper.wallpaperComponent : wallpaper.nextWallpaperComponent;
            if (!bindWallpaperComponentLocked(cname, true, DEBUG, wallpaper, reply)) {
                ServiceInfo si = null;
                try {
                    si = this.mIPackageManager.getServiceInfo(cname, DumpState.DUMP_DOMAIN_PREFERRED, wallpaper.userId);
                } catch (RemoteException e) {
                }
                if (si == null) {
                    Slog.w(TAG, "Failure starting previous wallpaper; clearing");
                    clearWallpaperLocked(DEBUG, 1, wallpaper.userId, reply);
                } else {
                    Slog.w(TAG, "Wallpaper isn't direct boot aware; using fallback until unlocked");
                    wallpaper.wallpaperComponent = wallpaper.nextWallpaperComponent;
                    WallpaperData fallback = new WallpaperData(wallpaper.userId, WALLPAPER_LOCK_ORIG, WALLPAPER_LOCK_CROP);
                    ensureSaneWallpaperData(fallback);
                    bindWallpaperComponentLocked(this.mImageWallpaper, true, DEBUG, fallback, reply);
                    this.mWaitingForUnlock = true;
                }
            }
        }
    }

    public void clearWallpaper(String callingPackage, int which, int userId) {
        checkPermission("android.permission.SET_WALLPAPER");
        if (isWallpaperSupported(callingPackage) && isSetWallpaperAllowed(callingPackage)) {
            userId = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, DEBUG, true, "clearWallpaper", null);
            synchronized (this.mLock) {
                clearWallpaperLocked(DEBUG, which, userId, null);
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
                loadSettingsLocked(userId, DEBUG);
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
                        wallpaper.imageWallpaperPending = DEBUG;
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
                        if (bindWallpaperComponentLocked(componentName, true, DEBUG, wallpaper, reply)) {
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
                            loadSettingsLocked(user.id, DEBUG);
                            wd = (WallpaperData) this.mWallpaperMap.get(user.id);
                        }
                        if (wd != null && name.equals(wd.name)) {
                            return true;
                        }
                    }
                }
                return DEBUG;
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
                width = Math.max(width, displaySize.x);
                height = Math.max(height, displaySize.y);
                if (!(width == wallpaper.width && height == wallpaper.height)) {
                    wallpaper.width = width;
                    wallpaper.height = height;
                    saveSettingsLocked(userId);
                    if (this.mCurrentUserId != userId) {
                        return;
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
                        return;
                    }
                }
                throw new IllegalArgumentException("padding must be positive: " + padding);
            }
        }
    }

    public ParcelFileDescriptor getWallpaper(IWallpaperManagerCallback cb, int which, Bundle outParams, int wallpaperUserId) {
        wallpaperUserId = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), wallpaperUserId, DEBUG, true, "getWallpaper", null);
        if (which == 1 || which == 2) {
            synchronized (this.mLock) {
                SparseArray<WallpaperData> whichSet = which == 2 ? this.mLockWallpaperMap : this.mWallpaperMap;
                WallpaperData wallpaper = (WallpaperData) whichSet.get(wallpaperUserId);
                if (wallpaper == null) {
                    loadSettingsLocked(wallpaperUserId, DEBUG);
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
                if (cb != null) {
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

    public WallpaperInfo getWallpaperInfo() {
        int userId = this.mCurrentUserId;
        synchronized (this.mLock) {
            WallpaperData wallpaper = (WallpaperData) this.mWallpaperMap.get(userId);
            if (wallpaper == null || wallpaper.connection == null) {
                return null;
            }
            WallpaperInfo wallpaperInfo = wallpaper.connection.mInfo;
            return wallpaperInfo;
        }
    }

    public int getWallpaperIdForUser(int which, int userId) {
        userId = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, DEBUG, true, "getWallpaperIdForUser", null);
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

    public ParcelFileDescriptor setWallpaper(String name, String callingPackage, Rect cropHint, boolean allowBackup, Bundle extras, int which, IWallpaperManagerCallback completion) {
        checkPermission("android.permission.SET_WALLPAPER");
        if ((which & 3) == 0) {
            String msg = "Must specify a valid wallpaper category to set";
            Slog.e(TAG, "Must specify a valid wallpaper category to set");
            throw new IllegalArgumentException("Must specify a valid wallpaper category to set");
        } else if (!isWallpaperSupported(callingPackage) || !isSetWallpaperAllowed(callingPackage)) {
            return null;
        } else {
            ParcelFileDescriptor pfd;
            if (cropHint == null) {
                cropHint = new Rect(0, 0, 0, 0);
            } else {
                if (!cropHint.isEmpty() && cropHint.left >= 0) {
                    if (cropHint.top < 0) {
                    }
                }
                throw new IllegalArgumentException("Invalid crop rect supplied: " + cropHint);
            }
            int userId = UserHandle.getCallingUserId();
            synchronized (this.mLock) {
                if (which == 1) {
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
                        if ((which & 1) != 0) {
                            wallpaper.allowBackup = allowBackup;
                        }
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
            lockWP.allowBackup = DEBUG;
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

    public void setWallpaperComponentChecked(ComponentName name, String callingPackage) {
        if (isWallpaperSupported(callingPackage) && isSetWallpaperAllowed(callingPackage)) {
            setWallpaperComponent(name);
        }
    }

    public void setWallpaperComponent(ComponentName name) {
        checkPermission("android.permission.SET_WALLPAPER_COMPONENT");
        synchronized (this.mLock) {
            int userId = UserHandle.getCallingUserId();
            WallpaperData wallpaper = (WallpaperData) this.mWallpaperMap.get(userId);
            if (wallpaper == null) {
                throw new IllegalStateException("Wallpaper not yet initialized for user " + userId);
            }
            long ident = Binder.clearCallingIdentity();
            try {
                wallpaper.imageWallpaperPending = DEBUG;
                handleWallpaperObserverEvent(wallpaper);
                if (bindWallpaperComponentLocked(name, DEBUG, true, wallpaper, null)) {
                    wallpaper.wallpaperId = makeWallpaperIdLocked();
                    notifyCallbacksLocked(wallpaper);
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    boolean bindWallpaperComponentLocked(ComponentName componentName, boolean force, boolean fromUser, WallpaperData wallpaper, IRemoteCallback reply) {
        String msg;
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
                componentName = WallpaperManager.getDefaultWallpaperComponent(this.mContext);
                if (componentName == null) {
                    componentName = this.mImageWallpaper;
                }
            } catch (XmlPullParserException e) {
                if (fromUser) {
                    throw new IllegalArgumentException(e);
                }
                Slog.w(TAG, e);
                return DEBUG;
            } catch (IOException e2) {
                if (fromUser) {
                    throw new IllegalArgumentException(e2);
                }
                Slog.w(TAG, e2);
                return DEBUG;
            } catch (RemoteException e3) {
                msg = "Remote exception for " + componentName + "\n" + e3;
                if (fromUser) {
                    throw new IllegalArgumentException(msg);
                }
                Slog.w(TAG, msg);
                return DEBUG;
            }
        }
        int serviceUserId = wallpaper.userId;
        ServiceInfo si = this.mIPackageManager.getServiceInfo(componentName, 4224, serviceUserId);
        if (si == null) {
            Slog.w(TAG, "Attempted wallpaper " + componentName + " is unavailable");
            return DEBUG;
        } else if ("android.permission.BIND_WALLPAPER".equals(si.permission)) {
            WallpaperInfo wallpaperInfo = null;
            Intent intent = new Intent("android.service.wallpaper.WallpaperService");
            if (componentName != null) {
                if (!componentName.equals(this.mImageWallpaper)) {
                    List<ResolveInfo> ris = this.mIPackageManager.queryIntentServices(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), MAX_WALLPAPER_COMPONENT_LOG_LENGTH, serviceUserId).getList();
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
                        return DEBUG;
                    }
                }
            }
            WallpaperConnection newConn = new WallpaperConnection(wallpaperInfo, wallpaper);
            intent.setComponent(componentName);
            intent.putExtra("android.intent.extra.client_label", 17040474);
            intent.putExtra("android.intent.extra.client_intent", PendingIntent.getActivityAsUser(this.mContext, 0, Intent.createChooser(new Intent("android.intent.action.SET_WALLPAPER"), this.mContext.getText(17040475)), 0, null, new UserHandle(serviceUserId)));
            if (this.mContext.bindServiceAsUser(intent, newConn, 570425345, new UserHandle(serviceUserId))) {
                if (wallpaper.userId == this.mCurrentUserId && this.mLastWallpaper != null) {
                    detachWallpaperLocked(this.mLastWallpaper);
                }
                wallpaper.wallpaperComponent = componentName;
                wallpaper.connection = newConn;
                newConn.mReply = reply;
                try {
                    if (wallpaper.userId == this.mCurrentUserId) {
                        this.mIWindowManager.addWindowToken(newConn.mToken, 2013);
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
            return DEBUG;
        } else {
            msg = "Selected service does not require android.permission.BIND_WALLPAPER: " + componentName;
            if (fromUser) {
                throw new SecurityException(msg);
            }
            Slog.w(TAG, msg);
            return DEBUG;
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
                this.mIWindowManager.removeWindowToken(wallpaper.connection.mToken);
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
            conn.mService.attach(conn, conn.mToken, 2013, DEBUG, wallpaper.width, wallpaper.height, wallpaper.padding);
        } catch (RemoteException e) {
            Slog.w(TAG, "Failed attaching wallpaper; clearing", e);
            if (!wallpaper.wallpaperUpdating) {
                bindWallpaperComponentLocked(null, DEBUG, DEBUG, wallpaper, null);
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
        return this.mAppOpsManager.checkOpNoThrow(48, Binder.getCallingUid(), callingPackage) == 0 ? true : DEBUG;
    }

    public boolean isSetWallpaperAllowed(String callingPackage) {
        boolean z = DEBUG;
        if (!Arrays.asList(this.mContext.getPackageManager().getPackagesForUid(Binder.getCallingUid())).contains(callingPackage)) {
            return DEBUG;
        }
        DevicePolicyManager dpm = (DevicePolicyManager) this.mContext.getSystemService(DevicePolicyManager.class);
        if (dpm.isDeviceOwnerApp(callingPackage) || dpm.isProfileOwnerApp(callingPackage)) {
            return true;
        }
        if (!((UserManager) this.mContext.getSystemService("user")).hasUserRestriction("no_set_wallpaper")) {
            z = true;
        }
        return z;
    }

    public boolean isWallpaperBackupEligible(int userId) {
        if (Binder.getCallingUid() != ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE) {
            throw new SecurityException("Only the system may call isWallpaperBackupEligible");
        }
        WallpaperData wallpaper = getWallpaperSafeLocked(userId, 1);
        return wallpaper != null ? wallpaper.allowBackup : DEBUG;
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
            FileOutputStream fstream = new FileOutputStream(journal.chooseForWrite(), DEBUG);
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
        if (!(wallpaper.wallpaperComponent == null || wallpaper.wallpaperComponent.equals(this.mImageWallpaper))) {
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
        loadSettingsLocked(userId, DEBUG);
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void loadSettingsLocked(int userId, boolean keepDimensionHints) {
        WallpaperData lockWallpaper;
        NullPointerException e;
        Object stream;
        NumberFormatException e2;
        XmlPullParserException e3;
        IOException e4;
        IndexOutOfBoundsException e5;
        AutoCloseable autoCloseable = null;
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
                generateCrop(wallpaperData);
            }
        }
        boolean success = DEBUG;
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
                            if (wallpaper.nextWallpaperComponent != null) {
                            }
                            wallpaper.nextWallpaperComponent = this.mImageWallpaper;
                        } else {
                            if ("kwp".equals(tag)) {
                                lockWallpaper = (WallpaperData) this.mLockWallpaperMap.get(userId);
                                if (lockWallpaper == null) {
                                    lockWallpaper = new WallpaperData(userId, WALLPAPER_LOCK_ORIG, WALLPAPER_LOCK_CROP);
                                    this.mLockWallpaperMap.put(userId, lockWallpaper);
                                }
                                parseWallpaperAttributes(parser, lockWallpaper, DEBUG);
                            }
                        }
                    }
                } while (type != 1);
                success = true;
                autoCloseable = fileInputStream;
            } catch (FileNotFoundException e6) {
                autoCloseable = fileInputStream;
            } catch (NullPointerException e7) {
                e = e7;
                stream = fileInputStream;
            } catch (NumberFormatException e8) {
                e2 = e8;
                stream = fileInputStream;
            } catch (XmlPullParserException e9) {
                e3 = e9;
                stream = fileInputStream;
            } catch (IOException e10) {
                e4 = e10;
                stream = fileInputStream;
            } catch (IndexOutOfBoundsException e11) {
                e5 = e11;
                stream = fileInputStream;
            }
        } catch (FileNotFoundException e12) {
            Slog.w(TAG, "no current wallpaper -- first boot?");
            IoUtils.closeQuietly(autoCloseable);
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
            if (lockWallpaper == null) {
                ensureSaneWallpaperData(lockWallpaper);
            }
        } catch (NullPointerException e13) {
            e = e13;
            Slog.w(TAG, "failed parsing " + file + " " + e);
            IoUtils.closeQuietly(autoCloseable);
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
            if (lockWallpaper == null) {
                ensureSaneWallpaperData(lockWallpaper);
            }
        } catch (NumberFormatException e14) {
            e2 = e14;
            Slog.w(TAG, "failed parsing " + file + " " + e2);
            IoUtils.closeQuietly(autoCloseable);
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
            if (lockWallpaper == null) {
                ensureSaneWallpaperData(lockWallpaper);
            }
        } catch (XmlPullParserException e15) {
            e3 = e15;
            Slog.w(TAG, "failed parsing " + file + " " + e3);
            IoUtils.closeQuietly(autoCloseable);
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
            if (lockWallpaper == null) {
                ensureSaneWallpaperData(lockWallpaper);
            }
        } catch (IOException e16) {
            e4 = e16;
            Slog.w(TAG, "failed parsing " + file + " " + e4);
            IoUtils.closeQuietly(autoCloseable);
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
            if (lockWallpaper == null) {
                ensureSaneWallpaperData(lockWallpaper);
            }
        } catch (IndexOutOfBoundsException e17) {
            e5 = e17;
            Slog.w(TAG, "failed parsing " + file + " " + e5);
            IoUtils.closeQuietly(autoCloseable);
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
            if (lockWallpaper == null) {
                ensureSaneWallpaperData(lockWallpaper);
            }
        }
        IoUtils.closeQuietly(autoCloseable);
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
        if (lockWallpaper == null) {
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
        if (Binder.getCallingUid() != ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE) {
            throw new RuntimeException("settingsRestored() can only be called from the system process");
        }
        synchronized (this.mLock) {
            boolean success;
            loadSettingsLocked(0, DEBUG);
            WallpaperData wallpaper = (WallpaperData) this.mWallpaperMap.get(0);
            wallpaper.wallpaperId = makeWallpaperIdLocked();
            wallpaper.allowBackup = true;
            if (wallpaper.nextWallpaperComponent == null || wallpaper.nextWallpaperComponent.equals(this.mImageWallpaper)) {
                if ("".equals(wallpaper.name)) {
                    success = true;
                } else {
                    success = restoreNamedResourceLocked(wallpaper);
                }
                if (success) {
                    generateCrop(wallpaper);
                    bindWallpaperComponentLocked(wallpaper.nextWallpaperComponent, true, DEBUG, wallpaper, null);
                }
            } else {
                if (!bindWallpaperComponentLocked(wallpaper.nextWallpaperComponent, DEBUG, DEBUG, wallpaper, null)) {
                    bindWallpaperComponentLocked(null, DEBUG, DEBUG, wallpaper, null);
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

    boolean restoreNamedResourceLocked(WallpaperData wallpaper) {
        Object fos;
        Throwable th;
        IOException e;
        Object cos;
        if (wallpaper.name.length() > 4) {
            if ("res:".equals(wallpaper.name.substring(0, 4))) {
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
                    int i = -1;
                    AutoCloseable autoCloseable = null;
                    AutoCloseable autoCloseable2 = null;
                    AutoCloseable autoCloseable3 = null;
                    try {
                        Resources r = this.mContext.createPackageContext(pkg, 4).getResources();
                        i = r.getIdentifier(resName, null, null);
                        if (i == 0) {
                            Slog.e(TAG, "couldn't resolve identifier pkg=" + pkg + " type=" + type + " ident=" + ident);
                            IoUtils.closeQuietly(null);
                            IoUtils.closeQuietly(null);
                            IoUtils.closeQuietly(null);
                            return DEBUG;
                        }
                        FileOutputStream cos2;
                        autoCloseable = r.openRawResource(i);
                        if (wallpaper.wallpaperFile.exists()) {
                            wallpaper.wallpaperFile.delete();
                            wallpaper.cropFile.delete();
                        }
                        FileOutputStream fos2 = new FileOutputStream(wallpaper.wallpaperFile);
                        try {
                            cos2 = new FileOutputStream(wallpaper.cropFile);
                        } catch (NameNotFoundException e2) {
                            fos = fos2;
                            try {
                                Slog.e(TAG, "Package name " + pkg + " not found");
                                IoUtils.closeQuietly(autoCloseable);
                                if (autoCloseable2 != null) {
                                    FileUtils.sync(autoCloseable2);
                                }
                                if (autoCloseable3 != null) {
                                    FileUtils.sync(autoCloseable3);
                                }
                                IoUtils.closeQuietly(autoCloseable2);
                                IoUtils.closeQuietly(autoCloseable3);
                                return DEBUG;
                            } catch (Throwable th2) {
                                th = th2;
                                IoUtils.closeQuietly(autoCloseable);
                                if (autoCloseable2 != null) {
                                    FileUtils.sync(autoCloseable2);
                                }
                                if (autoCloseable3 != null) {
                                    FileUtils.sync(autoCloseable3);
                                }
                                IoUtils.closeQuietly(autoCloseable2);
                                IoUtils.closeQuietly(autoCloseable3);
                                throw th;
                            }
                        } catch (NotFoundException e3) {
                            fos = fos2;
                            Slog.e(TAG, "Resource not found: " + i);
                            IoUtils.closeQuietly(autoCloseable);
                            if (autoCloseable2 != null) {
                                FileUtils.sync(autoCloseable2);
                            }
                            if (autoCloseable3 != null) {
                                FileUtils.sync(autoCloseable3);
                            }
                            IoUtils.closeQuietly(autoCloseable2);
                            IoUtils.closeQuietly(autoCloseable3);
                            return DEBUG;
                        } catch (IOException e4) {
                            e = e4;
                            fos = fos2;
                            Slog.e(TAG, "IOException while restoring wallpaper ", e);
                            IoUtils.closeQuietly(autoCloseable);
                            if (autoCloseable2 != null) {
                                FileUtils.sync(autoCloseable2);
                            }
                            if (autoCloseable3 != null) {
                                FileUtils.sync(autoCloseable3);
                            }
                            IoUtils.closeQuietly(autoCloseable2);
                            IoUtils.closeQuietly(autoCloseable3);
                            return DEBUG;
                        } catch (Throwable th3) {
                            th = th3;
                            fos = fos2;
                            IoUtils.closeQuietly(autoCloseable);
                            if (autoCloseable2 != null) {
                                FileUtils.sync(autoCloseable2);
                            }
                            if (autoCloseable3 != null) {
                                FileUtils.sync(autoCloseable3);
                            }
                            IoUtils.closeQuietly(autoCloseable2);
                            IoUtils.closeQuietly(autoCloseable3);
                            throw th;
                        }
                        try {
                            byte[] buffer = new byte[DumpState.DUMP_VERSION];
                            while (true) {
                                int amt = autoCloseable.read(buffer);
                                if (amt <= 0) {
                                    break;
                                }
                                fos2.write(buffer, 0, amt);
                                cos2.write(buffer, 0, amt);
                            }
                            Slog.v(TAG, "Restored wallpaper: " + resName);
                            IoUtils.closeQuietly(autoCloseable);
                            if (fos2 != null) {
                                FileUtils.sync(fos2);
                            }
                            if (cos2 != null) {
                                FileUtils.sync(cos2);
                            }
                            IoUtils.closeQuietly(fos2);
                            IoUtils.closeQuietly(cos2);
                            return true;
                        } catch (NameNotFoundException e5) {
                            autoCloseable3 = cos2;
                            autoCloseable2 = fos2;
                            Slog.e(TAG, "Package name " + pkg + " not found");
                            IoUtils.closeQuietly(autoCloseable);
                            if (autoCloseable2 != null) {
                                FileUtils.sync(autoCloseable2);
                            }
                            if (autoCloseable3 != null) {
                                FileUtils.sync(autoCloseable3);
                            }
                            IoUtils.closeQuietly(autoCloseable2);
                            IoUtils.closeQuietly(autoCloseable3);
                            return DEBUG;
                        } catch (NotFoundException e6) {
                            cos = cos2;
                            fos = fos2;
                            Slog.e(TAG, "Resource not found: " + i);
                            IoUtils.closeQuietly(autoCloseable);
                            if (autoCloseable2 != null) {
                                FileUtils.sync(autoCloseable2);
                            }
                            if (autoCloseable3 != null) {
                                FileUtils.sync(autoCloseable3);
                            }
                            IoUtils.closeQuietly(autoCloseable2);
                            IoUtils.closeQuietly(autoCloseable3);
                            return DEBUG;
                        } catch (IOException e7) {
                            e = e7;
                            cos = cos2;
                            fos = fos2;
                            Slog.e(TAG, "IOException while restoring wallpaper ", e);
                            IoUtils.closeQuietly(autoCloseable);
                            if (autoCloseable2 != null) {
                                FileUtils.sync(autoCloseable2);
                            }
                            if (autoCloseable3 != null) {
                                FileUtils.sync(autoCloseable3);
                            }
                            IoUtils.closeQuietly(autoCloseable2);
                            IoUtils.closeQuietly(autoCloseable3);
                            return DEBUG;
                        } catch (Throwable th4) {
                            th = th4;
                            cos = cos2;
                            fos = fos2;
                            IoUtils.closeQuietly(autoCloseable);
                            if (autoCloseable2 != null) {
                                FileUtils.sync(autoCloseable2);
                            }
                            if (autoCloseable3 != null) {
                                FileUtils.sync(autoCloseable3);
                            }
                            IoUtils.closeQuietly(autoCloseable2);
                            IoUtils.closeQuietly(autoCloseable3);
                            throw th;
                        }
                    } catch (NameNotFoundException e8) {
                        Slog.e(TAG, "Package name " + pkg + " not found");
                        IoUtils.closeQuietly(autoCloseable);
                        if (autoCloseable2 != null) {
                            FileUtils.sync(autoCloseable2);
                        }
                        if (autoCloseable3 != null) {
                            FileUtils.sync(autoCloseable3);
                        }
                        IoUtils.closeQuietly(autoCloseable2);
                        IoUtils.closeQuietly(autoCloseable3);
                        return DEBUG;
                    } catch (NotFoundException e9) {
                        Slog.e(TAG, "Resource not found: " + i);
                        IoUtils.closeQuietly(autoCloseable);
                        if (autoCloseable2 != null) {
                            FileUtils.sync(autoCloseable2);
                        }
                        if (autoCloseable3 != null) {
                            FileUtils.sync(autoCloseable3);
                        }
                        IoUtils.closeQuietly(autoCloseable2);
                        IoUtils.closeQuietly(autoCloseable3);
                        return DEBUG;
                    } catch (IOException e10) {
                        e = e10;
                        Slog.e(TAG, "IOException while restoring wallpaper ", e);
                        IoUtils.closeQuietly(autoCloseable);
                        if (autoCloseable2 != null) {
                            FileUtils.sync(autoCloseable2);
                        }
                        if (autoCloseable3 != null) {
                            FileUtils.sync(autoCloseable3);
                        }
                        IoUtils.closeQuietly(autoCloseable2);
                        IoUtils.closeQuietly(autoCloseable3);
                        return DEBUG;
                    }
                }
            }
        }
        return DEBUG;
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump wallpaper service from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            return;
        }
        synchronized (this.mLock) {
            int i;
            pw.println("System wallpaper state:");
            for (i = 0; i < this.mWallpaperMap.size(); i++) {
                WallpaperData wallpaper = (WallpaperData) this.mWallpaperMap.valueAt(i);
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
        return DEBUG;
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
}
