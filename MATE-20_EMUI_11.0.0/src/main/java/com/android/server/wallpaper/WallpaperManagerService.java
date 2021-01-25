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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Color;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.hdm.HwDeviceManager;
import android.hwtheme.HwThemeManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
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
import android.os.Trace;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.service.wallpaper.IWallpaperConnection;
import android.service.wallpaper.IWallpaperEngine;
import android.service.wallpaper.IWallpaperService;
import android.system.ErrnoException;
import android.system.Os;
import android.util.EventLog;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.Xml;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.IWindowManager;
import android.widget.Toast;
import com.android.internal.content.PackageMonitor;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.JournaledFile;
import com.android.server.BatteryService;
import com.android.server.EventLogTags;
import com.android.server.FgThread;
import com.android.server.HwServiceExFactory;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.pm.DumpState;
import com.android.server.pm.PackageManagerService;
import com.android.server.wallpaper.WallpaperManagerService;
import com.android.server.wm.WindowManagerInternal;
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
import java.util.function.Consumer;
import java.util.function.Predicate;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class WallpaperManagerService extends IWallpaperManager.Stub implements IWallpaperManagerService, IHwWallpaperManagerInner {
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_LIVE = true;
    private static final int MAX_BITMAP_SIZE = 104857600;
    private static final int MAX_WALLPAPER_COMPONENT_LOG_LENGTH = 128;
    private static final long MIN_WALLPAPER_CRASH_TIME = 10000;
    private static final Object SCREEN_SHOT_LIVE_WALLPAPER_LOCK = new Object();
    private static final String TAG = "WallpaperManagerService";
    static final String WALLPAPER = "wallpaper_orig";
    static final String WALLPAPER_CROP = "wallpaper";
    static final String WALLPAPER_INFO = "wallpaper_info.xml";
    static final String WALLPAPER_LOCK_CROP = "wallpaper_lock";
    static final String WALLPAPER_LOCK_ORIG = "wallpaper_lock_orig";
    public static Handler mWHandler = new Handler(Looper.getMainLooper());
    private static final String[] sPerUserFiles = {WALLPAPER, WALLPAPER_CROP, WALLPAPER_LOCK_ORIG, WALLPAPER_LOCK_CROP, WALLPAPER_INFO};
    private final AppOpsManager mAppOpsManager;
    private WallpaperColors mCacheDefaultImageWallpaperColors;
    private final SparseArray<SparseArray<RemoteCallbackList<IWallpaperManagerCallback>>> mColorsChangedListeners;
    private final Context mContext;
    private int mCurrentUserId = -10000;
    ComponentName mDefaultWallpaperComponent;
    private SparseArray<DisplayData> mDisplayDatas = new SparseArray<>();
    private final DisplayManager.DisplayListener mDisplayListener = new DisplayManager.DisplayListener() {
        /* class com.android.server.wallpaper.WallpaperManagerService.AnonymousClass1 */

        @Override // android.hardware.display.DisplayManager.DisplayListener
        public void onDisplayAdded(int displayId) {
        }

        @Override // android.hardware.display.DisplayManager.DisplayListener
        public void onDisplayRemoved(int displayId) {
            synchronized (WallpaperManagerService.this.mLock) {
                if (WallpaperManagerService.this.mLastWallpaper != null) {
                    WallpaperData targetWallpaper = null;
                    if (WallpaperManagerService.this.mLastWallpaper.connection.containsDisplay(displayId)) {
                        targetWallpaper = WallpaperManagerService.this.mLastWallpaper;
                    } else if (WallpaperManagerService.this.mFallbackWallpaper.connection.containsDisplay(displayId)) {
                        targetWallpaper = WallpaperManagerService.this.mFallbackWallpaper;
                    }
                    if (targetWallpaper != null) {
                        WallpaperConnection.DisplayConnector connector = targetWallpaper.connection.getDisplayConnectorOrCreate(displayId);
                        if (connector != null) {
                            connector.disconnectLocked();
                            targetWallpaper.connection.removeDisplayConnector(displayId);
                            WallpaperManagerService.this.removeDisplayData(displayId);
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                }
                for (int i = WallpaperManagerService.this.mColorsChangedListeners.size() - 1; i >= 0; i--) {
                    ((SparseArray) WallpaperManagerService.this.mColorsChangedListeners.valueAt(i)).delete(displayId);
                }
            }
        }

        @Override // android.hardware.display.DisplayManager.DisplayListener
        public void onDisplayChanged(int displayId) {
        }
    };
    private final DisplayManager mDisplayManager;
    private WallpaperData mFallbackWallpaper;
    private IHwWallpaperManagerServiceEx mIHwWMEx = null;
    private final IPackageManager mIPackageManager;
    private final IWindowManager mIWindowManager;
    private final ComponentName mImageWallpaper;
    private boolean mInAmbientMode;
    boolean mIsLoadLiveWallpaper = false;
    private IWallpaperManagerCallback mKeyguardListener;
    private WallpaperData mLastWallpaper;
    private final Object mLock = new Object();
    private final SparseArray<WallpaperData> mLockWallpaperMap = new SparseArray<>();
    private final MyPackageMonitor mMonitor;
    private boolean mShuttingDown;
    boolean mSuccess = false;
    private final SparseBooleanArray mUserRestorecon = new SparseBooleanArray();
    private boolean mWaitingForUnlock;
    private int mWallpaperId;
    private final SparseArray<WallpaperData> mWallpaperMap = new SparseArray<>();
    private final WindowManagerInternal mWindowManagerInternal;

    public static class Lifecycle extends SystemService {
        private IWallpaperManagerService mService;

        public Lifecycle(Context context) {
            super(context);
        }

        @Override // com.android.server.SystemService
        public void onStart() {
            try {
                this.mService = (IWallpaperManagerService) Class.forName(getContext().getResources().getString(17039895)).getConstructor(Context.class).newInstance(getContext());
                publishBinderService(WallpaperManagerService.WALLPAPER_CROP, this.mService);
            } catch (Exception exp) {
                Slog.wtf(WallpaperManagerService.TAG, "Failed to instantiate WallpaperManagerService", exp);
            }
        }

        @Override // com.android.server.SystemService
        public void onBootPhase(int phase) {
            IWallpaperManagerService iWallpaperManagerService = this.mService;
            if (iWallpaperManagerService != null) {
                iWallpaperManagerService.onBootPhase(phase);
            }
        }

        @Override // com.android.server.SystemService
        public void onUnlockUser(int userHandle) {
            IWallpaperManagerService iWallpaperManagerService = this.mService;
            if (iWallpaperManagerService != null) {
                iWallpaperManagerService.onUnlockUser(userHandle);
            }
        }
    }

    /* access modifiers changed from: private */
    public class WallpaperObserver extends FileObserver {
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
                        wallpaper = (WallpaperData) WallpaperManagerService.this.mLockWallpaperMap.get(this.mUserId);
                    } catch (Throwable th) {
                        throw th;
                    }
                }
                if (wallpaper == null) {
                    wallpaper = (WallpaperData) WallpaperManagerService.this.mWallpaperMap.get(this.mUserId);
                }
            }
            return wallpaper != null ? wallpaper : this.mWallpaper;
        }

        @Override // android.os.FileObserver
        public void onEvent(int event, String path) {
            WallpaperData wallpaper;
            Throwable th;
            int i;
            if (path != null) {
                boolean moved = event == 128;
                boolean written = event == 8 || moved;
                File changedFile = new File(this.mWallpaperDir, path);
                boolean sysWallpaperChanged = this.mWallpaperFile.equals(changedFile);
                boolean lockWallpaperChanged = this.mWallpaperLockFile.equals(changedFile);
                int notifyColorsWhich = 0;
                WallpaperData wallpaper2 = dataForEvent(sysWallpaperChanged, lockWallpaperChanged);
                if (!moved || !lockWallpaperChanged) {
                    synchronized (WallpaperManagerService.this.mLock) {
                        if (sysWallpaperChanged || lockWallpaperChanged) {
                            try {
                                WallpaperManagerService.this.mIHwWMEx.updateWallpaperOffsets(this.mWallpaper);
                                WallpaperManagerService.this.mIHwWMEx.handleWallpaperObserverEvent(this.mWallpaper);
                                WallpaperManagerService.this.notifyCallbacksLocked(wallpaper2, (lockWallpaperChanged ? 2 : 0) | 0 | sysWallpaperChanged);
                                if (wallpaper2.wallpaperComponent != null && event == 8) {
                                    try {
                                        if (!wallpaper2.imageWallpaperPending) {
                                            wallpaper = wallpaper2;
                                        }
                                    } catch (Throwable th2) {
                                        th = th2;
                                        throw th;
                                    }
                                }
                                if (written) {
                                    SELinux.restorecon(changedFile);
                                    if (moved) {
                                        SELinux.restorecon(changedFile);
                                        WallpaperManagerService.this.loadSettingsLocked(wallpaper2.userId, true);
                                    }
                                    WallpaperManagerService.this.generateCrop(wallpaper2);
                                    wallpaper2.imageWallpaperPending = false;
                                    if (sysWallpaperChanged != 0) {
                                        i = 2;
                                        wallpaper = wallpaper2;
                                        try {
                                            WallpaperManagerService.this.bindWallpaperComponentLocked(WallpaperManagerService.this.mImageWallpaper, true, false, wallpaper, null);
                                            notifyColorsWhich = 0 | 1;
                                        } catch (Throwable th3) {
                                            th = th3;
                                            throw th;
                                        }
                                    } else {
                                        i = 2;
                                        wallpaper = wallpaper2;
                                    }
                                    if (lockWallpaperChanged || (i & wallpaper.whichPending) != 0) {
                                        if (!lockWallpaperChanged) {
                                            WallpaperManagerService.this.mLockWallpaperMap.remove(wallpaper.userId);
                                        }
                                        WallpaperManagerService.this.notifyLockWallpaperChanged();
                                        notifyColorsWhich |= 2;
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
                            } catch (Throwable th4) {
                                th = th4;
                                throw th;
                            }
                        } else {
                            wallpaper = wallpaper2;
                        }
                    }
                    if (notifyColorsWhich != 0) {
                        WallpaperManagerService.this.notifyWallpaperColorsChanged(wallpaper, notifyColorsWhich);
                        return;
                    }
                    return;
                }
                SELinux.restorecon(changedFile);
                WallpaperManagerService.this.notifyLockWallpaperChanged();
                WallpaperManagerService.this.notifyWallpaperColorsChanged(wallpaper2, 2);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyLockWallpaperChanged() {
        IWallpaperManagerCallback cb = this.mKeyguardListener;
        if (cb != null) {
            try {
                cb.onWallpaperChanged();
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyWallpaperColorsChanged(WallpaperData wallpaper, int which) {
        if (wallpaper.connection != null) {
            wallpaper.connection.forEachDisplayConnector(new Consumer(wallpaper, which) {
                /* class com.android.server.wallpaper.$$Lambda$WallpaperManagerService$la7x4YHAl88Cd6HFTscnLBbKfI */
                private final /* synthetic */ WallpaperManagerService.WallpaperData f$1;
                private final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    WallpaperManagerService.this.lambda$notifyWallpaperColorsChanged$0$WallpaperManagerService(this.f$1, this.f$2, (WallpaperManagerService.WallpaperConnection.DisplayConnector) obj);
                }
            });
        } else {
            notifyWallpaperColorsChangedOnDisplay(wallpaper, which, 0);
        }
    }

    public /* synthetic */ void lambda$notifyWallpaperColorsChanged$0$WallpaperManagerService(WallpaperData wallpaper, int which, WallpaperConnection.DisplayConnector connector) {
        notifyWallpaperColorsChangedOnDisplay(wallpaper, which, connector.mDisplayId);
    }

    private RemoteCallbackList<IWallpaperManagerCallback> getWallpaperCallbacks(int userId, int displayId) {
        SparseArray<RemoteCallbackList<IWallpaperManagerCallback>> displayListeners = this.mColorsChangedListeners.get(userId);
        if (displayListeners != null) {
            return displayListeners.get(displayId);
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyWallpaperColorsChangedOnDisplay(WallpaperData wallpaper, int which, int displayId) {
        boolean needsExtraction;
        synchronized (this.mLock) {
            RemoteCallbackList<IWallpaperManagerCallback> currentUserColorListeners = getWallpaperCallbacks(wallpaper.userId, displayId);
            RemoteCallbackList<IWallpaperManagerCallback> userAllColorListeners = getWallpaperCallbacks(-1, displayId);
            if (!emptyCallbackList(currentUserColorListeners) || !emptyCallbackList(userAllColorListeners)) {
                needsExtraction = wallpaper.primaryColors == null;
            } else {
                return;
            }
        }
        notifyColorListeners(wallpaper.primaryColors, which, wallpaper.userId, displayId);
        if (needsExtraction) {
            extractColors(wallpaper);
            synchronized (this.mLock) {
                if (wallpaper.primaryColors != null) {
                    notifyColorListeners(wallpaper.primaryColors, which, wallpaper.userId, displayId);
                }
            }
        }
    }

    private static <T extends IInterface> boolean emptyCallbackList(RemoteCallbackList<T> list) {
        return list == null || list.getRegisteredCallbackCount() == 0;
    }

    private void notifyColorListeners(WallpaperColors wallpaperColors, int which, int userId, int displayId) {
        IWallpaperManagerCallback keyguardListener;
        ArrayList<IWallpaperManagerCallback> colorListeners = new ArrayList<>();
        synchronized (this.mLock) {
            RemoteCallbackList<IWallpaperManagerCallback> currentUserColorListeners = getWallpaperCallbacks(userId, displayId);
            RemoteCallbackList<IWallpaperManagerCallback> userAllColorListeners = getWallpaperCallbacks(-1, displayId);
            keyguardListener = this.mKeyguardListener;
            if (currentUserColorListeners != null) {
                int count = currentUserColorListeners.beginBroadcast();
                for (int i = 0; i < count; i++) {
                    colorListeners.add(currentUserColorListeners.getBroadcastItem(i));
                }
                currentUserColorListeners.finishBroadcast();
            }
            if (userAllColorListeners != null) {
                int count2 = userAllColorListeners.beginBroadcast();
                for (int i2 = 0; i2 < count2; i2++) {
                    colorListeners.add(userAllColorListeners.getBroadcastItem(i2));
                }
                userAllColorListeners.finishBroadcast();
            }
        }
        int count3 = colorListeners.size();
        for (int i3 = 0; i3 < count3; i3++) {
            try {
                colorListeners.get(i3).onWallpaperColorsChanged(wallpaperColors, which, userId);
            } catch (RemoteException e) {
            }
        }
        if (keyguardListener != null && displayId == 0) {
            try {
                keyguardListener.onWallpaperColorsChanged(wallpaperColors, which, userId);
            } catch (RemoteException e2) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:55:0x0083  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x008b  */
    private void extractColors(WallpaperData wallpaper) {
        boolean imageWallpaper;
        int wallpaperId;
        WallpaperColors colors;
        String cropFile = null;
        boolean defaultImageWallpaper = false;
        if (wallpaper.equals(this.mFallbackWallpaper)) {
            synchronized (this.mLock) {
                if (this.mFallbackWallpaper.primaryColors == null) {
                    WallpaperColors colors2 = extractDefaultImageWallpaperColors();
                    synchronized (this.mLock) {
                        this.mFallbackWallpaper.primaryColors = colors2;
                    }
                    return;
                }
                return;
            }
        }
        synchronized (this.mLock) {
            if (!this.mImageWallpaper.equals(wallpaper.wallpaperComponent)) {
                if (wallpaper.wallpaperComponent != null) {
                    imageWallpaper = false;
                    if (!imageWallpaper && wallpaper.cropFile != null && wallpaper.cropFile.exists()) {
                        cropFile = wallpaper.cropFile.getAbsolutePath();
                    } else if (imageWallpaper && !wallpaper.cropExists() && !wallpaper.sourceExists()) {
                        defaultImageWallpaper = true;
                    }
                    wallpaperId = wallpaper.wallpaperId;
                }
            }
            imageWallpaper = true;
            if (!imageWallpaper) {
            }
            defaultImageWallpaper = true;
            wallpaperId = wallpaper.wallpaperId;
        }
        WallpaperColors colors3 = null;
        if (cropFile != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(cropFile);
            if (bitmap != null) {
                colors3 = WallpaperColors.fromBitmap(bitmap);
                bitmap.recycle();
            }
        } else if (defaultImageWallpaper) {
            colors = extractDefaultImageWallpaperColors();
            if (colors != null) {
                Slog.w(TAG, "Cannot extract colors because wallpaper could not be read.");
                return;
            }
            synchronized (this.mLock) {
                if (wallpaper.wallpaperId == wallpaperId) {
                    wallpaper.primaryColors = colors;
                    saveSettingsLocked(wallpaper.userId);
                } else {
                    Slog.w(TAG, "Not setting primary colors since wallpaper changed");
                }
            }
            return;
        }
        colors = colors3;
        if (colors != null) {
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:0x003e, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x003f, code lost:
        if (r1 != null) goto L_0x0041;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0045, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0046, code lost:
        r2.addSuppressed(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0049, code lost:
        throw r3;
     */
    private WallpaperColors extractDefaultImageWallpaperColors() {
        WallpaperColors colors;
        synchronized (this.mLock) {
            if (this.mCacheDefaultImageWallpaperColors != null) {
                return this.mCacheDefaultImageWallpaperColors;
            }
        }
        WallpaperColors colors2 = null;
        try {
            InputStream is = WallpaperManager.openDefaultWallpaper(this.mContext, 1);
            if (is == null) {
                Slog.w(TAG, "Can't open default wallpaper stream");
                if (is != null) {
                    is.close();
                }
                return null;
            }
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, new BitmapFactory.Options());
            if (bitmap != null) {
                colors2 = WallpaperColors.fromBitmap(bitmap);
                bitmap.recycle();
            }
            is.close();
            colors = colors2;
            if (colors == null) {
                Slog.e(TAG, "Extract default image wallpaper colors failed");
            } else {
                synchronized (this.mLock) {
                    this.mCacheDefaultImageWallpaperColors = colors;
                }
            }
            return colors;
        } catch (OutOfMemoryError e) {
            Slog.w(TAG, "Can't decode default wallpaper stream", e);
        } catch (IOException e2) {
            Slog.w(TAG, "Can't close default wallpaper stream", e2);
            colors = null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:109:0x028e  */
    /* JADX WARNING: Removed duplicated region for block: B:112:0x02a0  */
    /* JADX WARNING: Removed duplicated region for block: B:115:? A[RETURN, SYNTHETIC] */
    private void generateCrop(WallpaperData wallpaper) {
        boolean success;
        Throwable th;
        boolean success2 = false;
        DisplayData wpData = getDisplayDataOrCreate(0);
        Rect cropHint = new Rect(wallpaper.cropHint);
        DisplayInfo displayInfo = new DisplayInfo();
        this.mDisplayManager.getDisplay(0).getDisplayInfo(displayInfo);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(wallpaper.wallpaperFile.getAbsolutePath(), options);
        if (options.outWidth > 0) {
            if (options.outHeight > 0) {
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
                    needCrop = options.outHeight > cropHint.height() || options.outWidth > cropHint.width();
                }
                boolean needScale = wpData.mHeight != (cropHint.height() > cropHint.width() ? cropHint.height() : cropHint.width()) || cropHint.height() > GLHelper.getMaxTextureSize() || cropHint.width() > GLHelper.getMaxTextureSize();
                if (needScale && wpData.mWidth == cropHint.width() && wpData.mHeight == cropHint.height()) {
                    needScale = false;
                    Slog.v(TAG, "same size wallpaper ,not need to scale.  w=" + cropHint.width() + " h=" + cropHint.height());
                }
                if (needScale) {
                    if (((int) (((float) cropHint.width()) * (((float) wpData.mHeight) / ((float) cropHint.height())))) < displayInfo.logicalWidth) {
                        cropHint.bottom = (int) (((float) cropHint.width()) * (((float) displayInfo.logicalHeight) / ((float) displayInfo.logicalWidth)));
                        needCrop = true;
                    }
                }
                if (needCrop || needScale) {
                    FileOutputStream f = null;
                    BufferedOutputStream bos = null;
                    try {
                        BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(wallpaper.wallpaperFile.getAbsolutePath(), false);
                        int scale = 1;
                        while (scale * 2 <= cropHint.height() / wpData.mHeight) {
                            scale *= 2;
                        }
                        options.inSampleSize = scale;
                        options.inJustDecodeBounds = false;
                        Rect estimateCrop = new Rect(cropHint);
                        estimateCrop.scale(1.0f / ((float) options.inSampleSize));
                        success = false;
                        try {
                            float hRatio = ((float) wpData.mHeight) / ((float) estimateCrop.height());
                            int height = (int) (((float) estimateCrop.height()) * hRatio);
                            try {
                                if (((int) (((float) estimateCrop.width()) * hRatio)) > GLHelper.getMaxTextureSize()) {
                                    int newHeight = (int) (((float) wpData.mHeight) / hRatio);
                                    int newWidth = (int) (((float) wpData.mWidth) / hRatio);
                                    estimateCrop.set(cropHint);
                                    try {
                                        estimateCrop.left += (cropHint.width() - newWidth) / 2;
                                        estimateCrop.top += (cropHint.height() - newHeight) / 2;
                                        estimateCrop.right = estimateCrop.left + newWidth;
                                        estimateCrop.bottom = estimateCrop.top + newHeight;
                                        cropHint.set(estimateCrop);
                                        estimateCrop.scale(1.0f / ((float) options.inSampleSize));
                                    } catch (Exception e) {
                                        IoUtils.closeQuietly((AutoCloseable) null);
                                        IoUtils.closeQuietly((AutoCloseable) null);
                                        success2 = success;
                                        if (!success2) {
                                        }
                                        if (wallpaper.cropFile.exists()) {
                                        }
                                    } catch (Throwable th2) {
                                        th = th2;
                                        IoUtils.closeQuietly((AutoCloseable) null);
                                        IoUtils.closeQuietly((AutoCloseable) null);
                                        throw th;
                                    }
                                }
                                int safeHeight = (int) (((float) estimateCrop.height()) * hRatio);
                                int safeWidth = (int) (((float) estimateCrop.width()) * hRatio);
                                Bitmap cropped = decoder.decodeRegion(cropHint, options);
                                decoder.recycle();
                                if (cropped == null) {
                                    Slog.e(TAG, "Could not decode new wallpaper");
                                    success2 = false;
                                } else {
                                    Bitmap finalCrop = Bitmap.createScaledBitmap(cropped, safeWidth, safeHeight, true);
                                    if (finalCrop.getByteCount() <= MAX_BITMAP_SIZE) {
                                        f = new FileOutputStream(wallpaper.cropFile);
                                        bos = new BufferedOutputStream(f, 32768);
                                        finalCrop.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                                        bos.flush();
                                        success2 = true;
                                    } else {
                                        throw new RuntimeException("Too large bitmap, limit=104857600");
                                    }
                                }
                                IoUtils.closeQuietly(bos);
                                IoUtils.closeQuietly(f);
                            } catch (Exception e2) {
                                IoUtils.closeQuietly((AutoCloseable) null);
                                IoUtils.closeQuietly((AutoCloseable) null);
                                success2 = success;
                                if (!success2) {
                                }
                                if (wallpaper.cropFile.exists()) {
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                IoUtils.closeQuietly((AutoCloseable) null);
                                IoUtils.closeQuietly((AutoCloseable) null);
                                throw th;
                            }
                        } catch (Exception e3) {
                            IoUtils.closeQuietly((AutoCloseable) null);
                            IoUtils.closeQuietly((AutoCloseable) null);
                            success2 = success;
                            if (!success2) {
                            }
                            if (wallpaper.cropFile.exists()) {
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            IoUtils.closeQuietly((AutoCloseable) null);
                            IoUtils.closeQuietly((AutoCloseable) null);
                            throw th;
                        }
                    } catch (Exception e4) {
                        success = false;
                        IoUtils.closeQuietly((AutoCloseable) null);
                        IoUtils.closeQuietly((AutoCloseable) null);
                        success2 = success;
                        if (!success2) {
                        }
                        if (wallpaper.cropFile.exists()) {
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        IoUtils.closeQuietly((AutoCloseable) null);
                        IoUtils.closeQuietly((AutoCloseable) null);
                        throw th;
                    }
                    if (!success2) {
                        Slog.e(TAG, "Unable to apply new wallpaper");
                        wallpaper.cropFile.delete();
                    }
                    if (wallpaper.cropFile.exists()) {
                        SELinux.restorecon(wallpaper.cropFile.getAbsoluteFile());
                        return;
                    }
                    return;
                }
                if (((long) (options.outWidth * options.outHeight * 4)) < 104857600) {
                    success2 = FileUtils.copyFile(wallpaper.wallpaperFile, wallpaper.cropFile);
                }
                if (!success2) {
                    wallpaper.cropFile.delete();
                }
                if (!success2) {
                }
                if (wallpaper.cropFile.exists()) {
                }
            }
        }
        Slog.w(TAG, "Invalid wallpaper data");
        success2 = false;
        if (!success2) {
        }
        if (wallpaper.cropFile.exists()) {
        }
    }

    public static class WallpaperData {
        boolean allowBackup;
        private RemoteCallbackList<IWallpaperManagerCallback> callbacks = new RemoteCallbackList<>();
        WallpaperConnection connection;
        final File cropFile;
        final Rect cropHint = new Rect(0, 0, 0, 0);
        public int[] currOffsets = {-1, -1, -1, -1};
        boolean imageWallpaperPending;
        long lastDiedTime;
        String name = "";
        public int[] nextOffsets = {-1, -1, -1, -1};
        ComponentName nextWallpaperComponent;
        WallpaperColors primaryColors;
        IWallpaperManagerCallback setComplete;
        int userId;
        ComponentName wallpaperComponent;
        final File wallpaperFile;
        int wallpaperId;
        WallpaperObserver wallpaperObserver;
        boolean wallpaperUpdating;
        int whichPending;

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
        }

        public int getWidth() {
            return -1;
        }

        public int getHeight() {
            return -1;
        }

        public RemoteCallbackList<IWallpaperManagerCallback> getCallbacks() {
            return this.callbacks;
        }
    }

    /* access modifiers changed from: private */
    public static final class DisplayData {
        final int mDisplayId;
        int mHeight = -1;
        final Rect mPadding = new Rect(0, 0, 0, 0);
        int mWidth = -1;

        DisplayData(int displayId) {
            this.mDisplayId = displayId;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeDisplayData(int displayId) {
        this.mDisplayDatas.remove(displayId);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private DisplayData getDisplayDataOrCreate(int displayId) {
        DisplayData wpdData = this.mDisplayDatas.get(displayId);
        if (wpdData != null) {
            return wpdData;
        }
        DisplayData wpdData2 = new DisplayData(displayId);
        ensureSaneWallpaperDisplaySize(wpdData2, displayId);
        this.mDisplayDatas.append(displayId, wpdData2);
        return wpdData2;
    }

    private void ensureSaneWallpaperDisplaySize(DisplayData wpdData, int displayId) {
        int baseSize = getMaximumSizeDimension(displayId);
        if (wpdData.mWidth < baseSize) {
            wpdData.mWidth = baseSize;
        }
        if (wpdData.mHeight < baseSize) {
            wpdData.mHeight = baseSize;
        }
    }

    private int getMaximumSizeDimension(int displayId) {
        Display display = this.mDisplayManager.getDisplay(displayId);
        if (display == null) {
            Slog.w(TAG, "Invalid displayId=" + displayId + " " + Debug.getCallers(4));
            display = this.mDisplayManager.getDisplay(0);
        }
        return display.getMaximumSizeDimension();
    }

    /* access modifiers changed from: package-private */
    public void forEachDisplayData(Consumer<DisplayData> action) {
        for (int i = this.mDisplayDatas.size() - 1; i >= 0; i--) {
            action.accept(this.mDisplayDatas.valueAt(i));
        }
    }

    /* access modifiers changed from: package-private */
    public int makeWallpaperIdLocked() {
        int i;
        do {
            this.mWallpaperId++;
            i = this.mWallpaperId;
        } while (i == 0);
        return i;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean supportsMultiDisplay(WallpaperConnection connection) {
        if (connection == null) {
            return false;
        }
        if (connection.mInfo == null || connection.mInfo.supportsMultipleDisplays()) {
            return true;
        }
        return false;
    }

    private void updateFallbackConnection() {
        WallpaperData wallpaperData = this.mLastWallpaper;
        if (wallpaperData != null && this.mFallbackWallpaper != null) {
            WallpaperConnection systemConnection = wallpaperData.connection;
            WallpaperConnection fallbackConnection = this.mFallbackWallpaper.connection;
            if (fallbackConnection == null) {
                Slog.w(TAG, "Fallback wallpaper connection has not been created yet!!");
            } else if (!supportsMultiDisplay(systemConnection)) {
                fallbackConnection.appendConnectorWithCondition(new Predicate() {
                    /* class com.android.server.wallpaper.$$Lambda$WallpaperManagerService$SxaUJpgTTfzUoz6u3AWuAOQdoNw */

                    @Override // java.util.function.Predicate
                    public final boolean test(Object obj) {
                        return WallpaperManagerService.lambda$updateFallbackConnection$2(WallpaperManagerService.WallpaperConnection.this, (Display) obj);
                    }
                });
                fallbackConnection.forEachDisplayConnector(new Consumer(fallbackConnection) {
                    /* class com.android.server.wallpaper.$$Lambda$WallpaperManagerService$tRb4SPHGj0pcxb3p7arcqKFqs08 */
                    private final /* synthetic */ WallpaperManagerService.WallpaperConnection f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        WallpaperManagerService.this.lambda$updateFallbackConnection$3$WallpaperManagerService(this.f$1, (WallpaperManagerService.WallpaperConnection.DisplayConnector) obj);
                    }
                });
            } else if (fallbackConnection.mDisplayConnector.size() != 0) {
                fallbackConnection.forEachDisplayConnector($$Lambda$WallpaperManagerService$pVmree9DyIpBSg0s3RDK3MDesvs.INSTANCE);
                fallbackConnection.mDisplayConnector.clear();
            }
        }
    }

    static /* synthetic */ void lambda$updateFallbackConnection$1(WallpaperConnection.DisplayConnector connector) {
        if (connector.mEngine != null) {
            connector.disconnectLocked();
        }
    }

    static /* synthetic */ boolean lambda$updateFallbackConnection$2(WallpaperConnection fallbackConnection, Display display) {
        return fallbackConnection.isUsableDisplay(display) && display.getDisplayId() != 0 && !fallbackConnection.containsDisplay(display.getDisplayId());
    }

    public /* synthetic */ void lambda$updateFallbackConnection$3$WallpaperManagerService(WallpaperConnection fallbackConnection, WallpaperConnection.DisplayConnector connector) {
        if (connector.mEngine == null) {
            connector.connectLocked(fallbackConnection, this.mFallbackWallpaper);
        }
    }

    /* access modifiers changed from: package-private */
    public class WallpaperConnection extends IWallpaperConnection.Stub implements ServiceConnection {
        private static final long WALLPAPER_RECONNECT_TIMEOUT_MS = 10000;
        final int mClientUid;
        private SparseArray<DisplayConnector> mDisplayConnector = new SparseArray<>();
        final WallpaperInfo mInfo;
        IRemoteCallback mReply;
        private Runnable mResetRunnable = new Runnable() {
            /* class com.android.server.wallpaper.$$Lambda$WallpaperManagerService$WallpaperConnection$QhODF3vswnwSYvDbeEhU85gOBw */

            @Override // java.lang.Runnable
            public final void run() {
                WallpaperManagerService.WallpaperConnection.this.lambda$new$0$WallpaperManagerService$WallpaperConnection();
            }
        };
        IWallpaperService mService;
        WallpaperData mWallpaper;

        /* access modifiers changed from: private */
        public final class DisplayConnector {
            boolean mDimensionsChanged;
            final int mDisplayId;
            IWallpaperEngine mEngine;
            boolean mPaddingChanged;
            final Binder mToken = new Binder();

            DisplayConnector(int displayId) {
                this.mDisplayId = displayId;
            }

            /* access modifiers changed from: package-private */
            public void ensureStatusHandled() {
                DisplayData wpdData = WallpaperManagerService.this.getDisplayDataOrCreate(this.mDisplayId);
                if (this.mDimensionsChanged) {
                    try {
                        this.mEngine.setDesiredSize(wpdData.mWidth, wpdData.mHeight);
                    } catch (RemoteException e) {
                        Slog.w(WallpaperManagerService.TAG, "Failed to set wallpaper dimensions", e);
                    }
                    this.mDimensionsChanged = false;
                }
                if (this.mPaddingChanged) {
                    try {
                        this.mEngine.setDisplayPadding(wpdData.mPadding);
                    } catch (RemoteException e2) {
                        Slog.w(WallpaperManagerService.TAG, "Failed to set wallpaper padding", e2);
                    }
                    this.mPaddingChanged = false;
                }
            }

            /* access modifiers changed from: package-private */
            public void connectLocked(WallpaperConnection connection, WallpaperData wallpaper) {
                if (connection.mService == null) {
                    Slog.w(WallpaperManagerService.TAG, "WallpaperService is not connected yet");
                    return;
                }
                try {
                    WallpaperManagerService.this.mIWindowManager.addWindowToken(this.mToken, 2013, this.mDisplayId);
                    DisplayData wpdData = WallpaperManagerService.this.getDisplayDataOrCreate(this.mDisplayId);
                    try {
                        connection.mService.attach(connection, this.mToken, 2013, false, wpdData.mWidth, wpdData.mHeight, wpdData.mPadding, this.mDisplayId);
                    } catch (RemoteException e) {
                        Slog.w(WallpaperManagerService.TAG, "Failed attaching wallpaper on display", e);
                        if (wallpaper != null && !wallpaper.wallpaperUpdating && connection.getConnectedEngineSize() == 0) {
                            WallpaperManagerService.this.bindWallpaperComponentLocked(null, false, false, wallpaper, null);
                        }
                    }
                } catch (RemoteException e2) {
                    Slog.e(WallpaperManagerService.TAG, "Failed add wallpaper window token on display " + this.mDisplayId, e2);
                }
            }

            /* access modifiers changed from: package-private */
            public void disconnectLocked() {
                try {
                    WallpaperManagerService.this.mIWindowManager.removeWindowToken(this.mToken, this.mDisplayId);
                } catch (RemoteException e) {
                }
                try {
                    if (this.mEngine != null) {
                        this.mEngine.destroy();
                    }
                } catch (RemoteException e2) {
                }
                this.mEngine = null;
            }
        }

        public /* synthetic */ void lambda$new$0$WallpaperManagerService$WallpaperConnection() {
            synchronized (WallpaperManagerService.this.mLock) {
                if (WallpaperManagerService.this.mShuttingDown) {
                    Slog.i(WallpaperManagerService.TAG, "Ignoring relaunch timeout during shutdown");
                    return;
                }
                if (!this.mWallpaper.wallpaperUpdating && this.mWallpaper.userId == WallpaperManagerService.this.mCurrentUserId) {
                    Slog.w(WallpaperManagerService.TAG, "Wallpaper reconnect timed out for " + this.mWallpaper.wallpaperComponent + ", reverting to built-in wallpaper!");
                    WallpaperManagerService.this.clearWallpaperLocked(true, 1, this.mWallpaper.userId, null);
                }
            }
        }

        WallpaperConnection(WallpaperInfo info, WallpaperData wallpaper, int clientUid) {
            this.mInfo = info;
            this.mWallpaper = wallpaper;
            this.mClientUid = clientUid;
            initDisplayState();
        }

        private void initDisplayState() {
            if (this.mWallpaper.equals(WallpaperManagerService.this.mFallbackWallpaper)) {
                return;
            }
            if (WallpaperManagerService.this.supportsMultiDisplay(this)) {
                appendConnectorWithCondition(new Predicate() {
                    /* class com.android.server.wallpaper.$$Lambda$WallpaperManagerService$WallpaperConnection$NrNkceFJLqjCb8eAxErUhpLd5c8 */

                    @Override // java.util.function.Predicate
                    public final boolean test(Object obj) {
                        return WallpaperManagerService.WallpaperConnection.this.isUsableDisplay((Display) obj);
                    }
                });
            } else {
                this.mDisplayConnector.append(0, new DisplayConnector(0));
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void appendConnectorWithCondition(Predicate<Display> tester) {
            Display[] displays = WallpaperManagerService.this.mDisplayManager.getDisplays();
            for (Display display : displays) {
                if (tester.test(display)) {
                    int displayId = display.getDisplayId();
                    if (this.mDisplayConnector.get(displayId) == null) {
                        this.mDisplayConnector.append(displayId, new DisplayConnector(displayId));
                    }
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        public boolean isUsableDisplay(Display display) {
            if (display == null || !display.hasAccess(this.mClientUid)) {
                return false;
            }
            int displayId = display.getDisplayId();
            if (displayId == 0) {
                return true;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                return WallpaperManagerService.this.mWindowManagerInternal.shouldShowSystemDecorOnDisplay(displayId);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        /* access modifiers changed from: package-private */
        public void forEachDisplayConnector(Consumer<DisplayConnector> action) {
            for (int i = this.mDisplayConnector.size() - 1; i >= 0; i--) {
                action.accept(this.mDisplayConnector.valueAt(i));
            }
        }

        /* access modifiers changed from: package-private */
        public int getConnectedEngineSize() {
            int engineSize = 0;
            for (int i = this.mDisplayConnector.size() - 1; i >= 0; i--) {
                if (this.mDisplayConnector.valueAt(i).mEngine != null) {
                    engineSize++;
                }
            }
            return engineSize;
        }

        /* access modifiers changed from: package-private */
        public DisplayConnector getDisplayConnectorOrCreate(int displayId) {
            DisplayConnector connector = this.mDisplayConnector.get(displayId);
            if (connector != null || !isUsableDisplay(WallpaperManagerService.this.mDisplayManager.getDisplay(displayId))) {
                return connector;
            }
            DisplayConnector connector2 = new DisplayConnector(displayId);
            this.mDisplayConnector.append(displayId, connector2);
            return connector2;
        }

        /* access modifiers changed from: package-private */
        public boolean containsDisplay(int displayId) {
            return this.mDisplayConnector.get(displayId) != null;
        }

        /* access modifiers changed from: package-private */
        public void removeDisplayConnector(int displayId) {
            if (this.mDisplayConnector.get(displayId) != null) {
                this.mDisplayConnector.remove(displayId);
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            Slog.i(WallpaperManagerService.TAG, "Service Connected: " + name + " userId:" + this.mWallpaper.userId);
            synchronized (WallpaperManagerService.this.mLock) {
                if (this.mWallpaper.connection == this) {
                    this.mService = IWallpaperService.Stub.asInterface(service);
                    WallpaperManagerService.this.attachServiceLocked(this, this.mWallpaper);
                    if (!this.mWallpaper.equals(WallpaperManagerService.this.mFallbackWallpaper)) {
                        WallpaperManagerService.this.saveSettingsLocked(this.mWallpaper.userId);
                    }
                    FgThread.getHandler().removeCallbacks(this.mResetRunnable);
                    if (name != null && !name.equals(WallpaperManagerService.this.mImageWallpaper)) {
                        WallpaperManagerService.this.mContext.sendBroadcastAsUser(new Intent("android.intent.action.WALLPAPER_CHANGED"), new UserHandle(WallpaperManagerService.this.mCurrentUserId));
                    }
                }
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            synchronized (WallpaperManagerService.this.mLock) {
                Slog.w(WallpaperManagerService.TAG, "Wallpaper service gone: " + name);
                if (!Objects.equals(name, this.mWallpaper.wallpaperComponent)) {
                    Slog.e(WallpaperManagerService.TAG, "Does not match expected wallpaper component " + this.mWallpaper.wallpaperComponent);
                }
                this.mService = null;
                forEachDisplayConnector($$Lambda$WallpaperManagerService$WallpaperConnection$87DhM3RJJxRNtgkHmd_gtnGkz4.INSTANCE);
                if (this.mWallpaper.connection == this && !this.mWallpaper.wallpaperUpdating) {
                    WallpaperManagerService.this.mContext.getMainThreadHandler().postDelayed(new Runnable() {
                        /* class com.android.server.wallpaper.$$Lambda$WallpaperManagerService$WallpaperConnection$Yk86TTURTI5B9DzxOzMQGDq7aQU */

                        @Override // java.lang.Runnable
                        public final void run() {
                            WallpaperManagerService.WallpaperConnection.this.lambda$onServiceDisconnected$2$WallpaperManagerService$WallpaperConnection();
                        }
                    }, 1000);
                }
            }
        }

        public /* synthetic */ void lambda$onServiceDisconnected$2$WallpaperManagerService$WallpaperConnection() {
            processDisconnect(this);
        }

        public void scheduleTimeoutLocked() {
            Handler fgHandler = FgThread.getHandler();
            fgHandler.removeCallbacks(this.mResetRunnable);
            fgHandler.postDelayed(this.mResetRunnable, 10000);
            Slog.i(WallpaperManagerService.TAG, "Started wallpaper reconnect timeout for " + this.mWallpaper.wallpaperComponent);
        }

        private void processDisconnect(ServiceConnection connection) {
            synchronized (WallpaperManagerService.this.mLock) {
                if (connection == this.mWallpaper.connection) {
                    ComponentName wpService = this.mWallpaper.wallpaperComponent;
                    String defaultLiveWallpaper = HwThemeManager.getDefaultLiveWallpaper(UserHandle.myUserId());
                    boolean isDefaultLiveWallpaper = defaultLiveWallpaper != null && Objects.equals(ComponentName.unflattenFromString(defaultLiveWallpaper), wpService);
                    if (!this.mWallpaper.wallpaperUpdating && this.mWallpaper.userId == WallpaperManagerService.this.mCurrentUserId && ((!Objects.equals(WallpaperManagerService.this.mDefaultWallpaperComponent, wpService) || isDefaultLiveWallpaper) && !Objects.equals(WallpaperManagerService.this.mImageWallpaper, wpService))) {
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
                        EventLog.writeEvent((int) EventLogTags.WP_WALLPAPER_CRASHED, flattened.substring(0, Math.min(flattened.length(), 128)));
                    }
                } else {
                    Slog.i(WallpaperManagerService.TAG, "Wallpaper changed during disconnect tracking; ignoring");
                }
            }
        }

        public void onWallpaperColorsChanged(WallpaperColors primaryColors, int displayId) {
            int which;
            synchronized (WallpaperManagerService.this.mLock) {
                if (!WallpaperManagerService.this.mImageWallpaper.equals(this.mWallpaper.wallpaperComponent)) {
                    this.mWallpaper.primaryColors = primaryColors;
                    which = 1;
                    if (displayId == 0 && ((WallpaperData) WallpaperManagerService.this.mLockWallpaperMap.get(this.mWallpaper.userId)) == null) {
                        which = 1 | 2;
                    }
                } else {
                    return;
                }
            }
            if (which != 0) {
                WallpaperManagerService.this.notifyWallpaperColorsChangedOnDisplay(this.mWallpaper, which, displayId);
            }
        }

        public void attachEngine(IWallpaperEngine engine, int displayId) {
            Slog.i(WallpaperManagerService.TAG, "attachEngine userId:" + this.mWallpaper.userId);
            synchronized (WallpaperManagerService.this.mLock) {
                DisplayConnector connector = getDisplayConnectorOrCreate(displayId);
                if (connector == null) {
                    try {
                        engine.destroy();
                    } catch (RemoteException e) {
                        Slog.w(WallpaperManagerService.TAG, "Failed to destroy engine", e);
                    }
                    return;
                }
                connector.mEngine = engine;
                connector.ensureStatusHandled();
                if (this.mInfo != null && this.mInfo.supportsAmbientMode() && displayId == 0) {
                    try {
                        connector.mEngine.setInAmbientMode(WallpaperManagerService.this.mInAmbientMode, 0);
                    } catch (RemoteException e2) {
                        Slog.w(WallpaperManagerService.TAG, "Failed to set ambient mode state", e2);
                    }
                }
                try {
                    connector.mEngine.requestWallpaperColors();
                } catch (RemoteException e3) {
                    Slog.w(WallpaperManagerService.TAG, "Failed to request wallpaper colors", e3);
                }
            }
        }

        public void engineShown(IWallpaperEngine engine) {
            synchronized (WallpaperManagerService.this.mLock) {
                if (this.mReply != null) {
                    long ident = Binder.clearCallingIdentity();
                    try {
                        this.mReply.sendResult((Bundle) null);
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
                return WallpaperManagerService.this.updateWallpaperBitmapLocked(name, this.mWallpaper, null);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class MyPackageMonitor extends PackageMonitor {
        static final String PACKAGE_SYSTEMUI = "com.android.systemui";

        MyPackageMonitor() {
        }

        public void onPackageUpdateFinished(String packageName, int uid) {
            ComponentName wpService;
            synchronized (WallpaperManagerService.this.mLock) {
                if (WallpaperManagerService.this.mCurrentUserId == getChangingUserId()) {
                    WallpaperData wallpaper = (WallpaperData) WallpaperManagerService.this.mWallpaperMap.get(WallpaperManagerService.this.mCurrentUserId);
                    if (!(wallpaper == null || (wpService = wallpaper.wallpaperComponent) == null || !wpService.getPackageName().equals(packageName))) {
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

        public void onPackageModified(String packageName) {
            synchronized (WallpaperManagerService.this.mLock) {
                if (WallpaperManagerService.this.mCurrentUserId == getChangingUserId()) {
                    WallpaperData wallpaper = (WallpaperData) WallpaperManagerService.this.mWallpaperMap.get(WallpaperManagerService.this.mCurrentUserId);
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

        public void onPackageUpdateStarted(String packageName, int uid) {
            synchronized (WallpaperManagerService.this.mLock) {
                if (WallpaperManagerService.this.mCurrentUserId == getChangingUserId()) {
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
        }

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
                WallpaperData wallpaper = (WallpaperData) WallpaperManagerService.this.mWallpaperMap.get(WallpaperManagerService.this.mCurrentUserId);
                if (wallpaper != null) {
                    changed = false | doPackagesChangedLocked(doit, wallpaper);
                }
                return changed;
            }
        }

        public void onSomePackagesChanged() {
            synchronized (WallpaperManagerService.this.mLock) {
                if (WallpaperManagerService.this.mCurrentUserId == getChangingUserId()) {
                    WallpaperData wallpaper = (WallpaperData) WallpaperManagerService.this.mWallpaperMap.get(WallpaperManagerService.this.mCurrentUserId);
                    if (wallpaper != null) {
                        doPackagesChangedLocked(true, wallpaper);
                    }
                }
            }
        }

        /* access modifiers changed from: package-private */
        public boolean doPackagesChangedLocked(boolean doit, WallpaperData wallpaper) {
            int change;
            int change2;
            boolean changed = false;
            if (wallpaper.wallpaperComponent != null && ((change2 = isPackageDisappearing(wallpaper.wallpaperComponent.getPackageName())) == 3 || change2 == 2)) {
                changed = true;
                if (doit) {
                    Slog.w(WallpaperManagerService.TAG, "Wallpaper uninstalled, removing: " + wallpaper.wallpaperComponent);
                    WallpaperManagerService.this.clearWallpaperLocked(false, 1, wallpaper.userId, null);
                    WallpaperManagerService.this.mContext.sendBroadcastAsUser(new Intent("android.intent.action.WALLPAPER_CHANGED"), new UserHandle(WallpaperManagerService.this.mCurrentUserId));
                }
            }
            if (wallpaper.nextWallpaperComponent != null && ((change = isPackageDisappearing(wallpaper.nextWallpaperComponent.getPackageName())) == 3 || change == 2)) {
                wallpaper.nextWallpaperComponent = null;
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

    public WallpaperManagerService(Context context) {
        this.mContext = context;
        this.mShuttingDown = false;
        this.mImageWallpaper = ComponentName.unflattenFromString(context.getResources().getString(17040281));
        this.mDefaultWallpaperComponent = WallpaperManager.getDefaultWallpaperComponent(context);
        this.mIWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
        this.mWindowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
        this.mIPackageManager = AppGlobals.getPackageManager();
        this.mAppOpsManager = (AppOpsManager) this.mContext.getSystemService("appops");
        this.mDisplayManager = (DisplayManager) this.mContext.getSystemService(DisplayManager.class);
        this.mDisplayManager.registerDisplayListener(this.mDisplayListener, null);
        this.mMonitor = new MyPackageMonitor();
        this.mColorsChangedListeners = new SparseArray<>();
        LocalServices.addService(WallpaperManagerInternal.class, new LocalService());
        this.mIHwWMEx = HwServiceExFactory.getHwWallpaperManagerServiceEx(this, context);
    }

    private final class LocalService extends WallpaperManagerInternal {
        private LocalService() {
        }

        @Override // com.android.server.wallpaper.WallpaperManagerInternal
        public void onDisplayReady(int displayId) {
            WallpaperManagerService.this.onDisplayReadyInternal(displayId);
        }
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
    @Override // java.lang.Object
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
            /* class com.android.server.wallpaper.WallpaperManagerService.AnonymousClass2 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.USER_REMOVED".equals(intent.getAction())) {
                    WallpaperManagerService.this.onRemoveUser(intent.getIntExtra("android.intent.extra.user_handle", -10000));
                }
            }
        }, userFilter);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.wallpaper.WallpaperManagerService.AnonymousClass3 */

            @Override // android.content.BroadcastReceiver
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
                /* class com.android.server.wallpaper.WallpaperManagerService.AnonymousClass4 */

                public void onUserSwitching(int newUserId, IRemoteCallback reply) {
                    WallpaperManagerService.this.switchUser(newUserId, reply);
                }
            }, TAG);
        } catch (RemoteException e) {
            e.rethrowAsRuntimeException();
        }
        this.mIHwWMEx.hwSystemReady();
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
        if (wallpaper != null && wallpaper.wallpaperObserver != null) {
            wallpaper.wallpaperObserver.stopWatching();
            wallpaper.wallpaperObserver = null;
        }
    }

    /* access modifiers changed from: package-private */
    public void stopObserversLocked(int userId) {
        stopObserver(this.mWallpaperMap.get(userId));
        stopObserver(this.mLockWallpaperMap.get(userId));
        this.mWallpaperMap.remove(userId);
        this.mLockWallpaperMap.remove(userId);
    }

    @Override // com.android.server.wallpaper.IWallpaperManagerService
    public void onBootPhase(int phase) {
        if (phase == 550) {
            systemReady();
        } else if (phase == 600) {
            switchUser(0, null);
        } else if (phase == 1000) {
            this.mIHwWMEx.wallpaperManagerServiceReady();
        }
    }

    @Override // com.android.server.wallpaper.IWallpaperManagerService
    public void onUnlockUser(final int userId) {
        synchronized (this.mLock) {
            if (this.mCurrentUserId == userId) {
                if (this.mWaitingForUnlock) {
                    WallpaperData systemWallpaper = getWallpaperSafeLocked(userId, 1);
                    switchWallpaper(systemWallpaper, null);
                    notifyCallbacksLocked(systemWallpaper, 1);
                }
                if (!this.mUserRestorecon.get(userId)) {
                    this.mUserRestorecon.put(userId, true);
                    BackgroundThread.getHandler().post(new Runnable() {
                        /* class com.android.server.wallpaper.WallpaperManagerService.AnonymousClass5 */

                        @Override // java.lang.Runnable
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
                this.mUserRestorecon.delete(userId);
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
                    this.mIHwWMEx.handleWallpaperObserverEvent(this.mLastWallpaper);
                }
                WallpaperData systemWallpaper = getWallpaperSafeLocked(userId, 1);
                WallpaperData tmpLockWallpaper = this.mLockWallpaperMap.get(userId);
                WallpaperData lockWallpaper = tmpLockWallpaper == null ? systemWallpaper : tmpLockWallpaper;
                if (systemWallpaper.wallpaperObserver == null) {
                    systemWallpaper.wallpaperObserver = new WallpaperObserver(systemWallpaper);
                    systemWallpaper.wallpaperObserver.startWatching();
                }
                switchWallpaper(systemWallpaper, reply);
                FgThread.getHandler().post(new Runnable(systemWallpaper, lockWallpaper) {
                    /* class com.android.server.wallpaper.$$Lambda$WallpaperManagerService$xeJGAwCI8tssclwKFf8jMsYdoKQ */
                    private final /* synthetic */ WallpaperManagerService.WallpaperData f$1;
                    private final /* synthetic */ WallpaperManagerService.WallpaperData f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        WallpaperManagerService.this.lambda$switchUser$4$WallpaperManagerService(this.f$1, this.f$2);
                    }
                });
            }
        }
    }

    public /* synthetic */ void lambda$switchUser$4$WallpaperManagerService(WallpaperData systemWallpaper, WallpaperData lockWallpaper) {
        notifyWallpaperColorsChanged(systemWallpaper, 1);
        notifyWallpaperColorsChanged(lockWallpaper, 2);
        notifyWallpaperColorsChanged(this.mFallbackWallpaper, 1);
    }

    /* access modifiers changed from: package-private */
    public void switchWallpaper(WallpaperData wallpaper, IRemoteCallback reply) {
        ServiceInfo si;
        synchronized (this.mLock) {
            try {
                this.mWaitingForUnlock = false;
                ComponentName cname = wallpaper.wallpaperComponent != null ? wallpaper.wallpaperComponent : wallpaper.nextWallpaperComponent;
                if (!bindWallpaperComponentLocked(cname, true, false, wallpaper, reply)) {
                    try {
                        si = this.mIPackageManager.getServiceInfo(cname, (int) DumpState.DUMP_DOMAIN_PREFERRED, wallpaper.userId);
                    } catch (RemoteException e) {
                        si = null;
                    }
                    if (si == null) {
                        Slog.w(TAG, "Failure starting previous wallpaper; clearing");
                        clearWallpaperLocked(false, 1, wallpaper.userId, reply);
                    } else {
                        Slog.w(TAG, "Wallpaper isn't direct boot aware; using fallback until unlocked");
                        wallpaper.wallpaperComponent = wallpaper.nextWallpaperComponent;
                        WallpaperData fallback = new WallpaperData(wallpaper.userId, WALLPAPER_LOCK_ORIG, WALLPAPER_LOCK_CROP);
                        ensureSaneWallpaperData(fallback, 0);
                        bindWallpaperComponentLocked(this.mImageWallpaper, true, false, fallback, reply);
                        this.mWaitingForUnlock = true;
                    }
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
                notifyWallpaperColorsChanged(this.mFallbackWallpaper, 1);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void clearWallpaperLocked(boolean defaultFailed, int which, int userId, IRemoteCallback reply) {
        WallpaperData wallpaper;
        ComponentName componentName;
        if (which == 1 || which == 2) {
            if (which == 2) {
                WallpaperData wallpaper2 = this.mLockWallpaperMap.get(userId);
                if (wallpaper2 != null) {
                    wallpaper = wallpaper2;
                } else {
                    return;
                }
            } else {
                WallpaperData wallpaper3 = this.mWallpaperMap.get(userId);
                if (wallpaper3 == null) {
                    loadSettingsLocked(userId, false);
                    wallpaper = this.mWallpaperMap.get(userId);
                } else {
                    wallpaper = wallpaper3;
                }
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
                    RuntimeException e2 = null;
                    try {
                        wallpaper.primaryColors = null;
                        wallpaper.imageWallpaperPending = false;
                        if (userId != this.mCurrentUserId) {
                            Binder.restoreCallingIdentity(ident);
                            return;
                        }
                        this.mDefaultWallpaperComponent = null;
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
                                reply.sendResult((Bundle) null);
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

    private boolean isValidDisplay(int displayId) {
        return this.mDisplayManager.getDisplay(displayId) != null;
    }

    public void setDimensionHints(int width, int height, String callingPackage, int displayId) throws RemoteException {
        checkPermission("android.permission.SET_WALLPAPER_HINTS");
        if (isWallpaperSupported(callingPackage)) {
            int width2 = Math.min(width, GLHelper.getMaxTextureSize());
            int height2 = Math.min(height, GLHelper.getMaxTextureSize());
            synchronized (this.mLock) {
                int userId = UserHandle.getCallingUserId();
                WallpaperData wallpaper = getWallpaperSafeLocked(userId, 1);
                if (width2 <= 0 || height2 <= 0) {
                    throw new IllegalArgumentException("width and height must be > 0");
                } else if (isValidDisplay(displayId)) {
                    DisplayData wpdData = getDisplayDataOrCreate(displayId);
                    if (!(width2 == wpdData.mWidth && height2 == wpdData.mHeight)) {
                        wpdData.mWidth = width2;
                        wpdData.mHeight = height2;
                        if (displayId == 0) {
                            saveSettingsLocked(userId);
                        }
                        if (this.mCurrentUserId == userId) {
                            if (wallpaper.connection != null) {
                                WallpaperConnection.DisplayConnector connector = wallpaper.connection.getDisplayConnectorOrCreate(displayId);
                                IWallpaperEngine engine = connector != null ? connector.mEngine : null;
                                if (engine != null) {
                                    try {
                                        engine.setDesiredSize(width2, height2);
                                    } catch (RemoteException e) {
                                    }
                                    notifyCallbacksLocked(wallpaper, 1);
                                } else if (!(wallpaper.connection.mService == null || connector == null)) {
                                    connector.mDimensionsChanged = true;
                                }
                            }
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Cannot find display with id=" + displayId);
                }
            }
        }
    }

    public int getWidthHint(int displayId) throws RemoteException {
        synchronized (this.mLock) {
            if (!isValidDisplay(displayId)) {
                throw new IllegalArgumentException("Cannot find display with id=" + displayId);
            } else if (this.mWallpaperMap.get(UserHandle.getCallingUserId()) == null) {
                return 0;
            } else {
                return getDisplayDataOrCreate(displayId).mWidth;
            }
        }
    }

    public int getHeightHint(int displayId) throws RemoteException {
        synchronized (this.mLock) {
            if (!isValidDisplay(displayId)) {
                throw new IllegalArgumentException("Cannot find display with id=" + displayId);
            } else if (this.mWallpaperMap.get(UserHandle.getCallingUserId()) == null) {
                return 0;
            } else {
                return getDisplayDataOrCreate(displayId).mHeight;
            }
        }
    }

    public void setDisplayPadding(Rect padding, String callingPackage, int displayId) {
        checkPermission("android.permission.SET_WALLPAPER_HINTS");
        if (isWallpaperSupported(callingPackage)) {
            synchronized (this.mLock) {
                if (isValidDisplay(displayId)) {
                    int userId = UserHandle.getCallingUserId();
                    WallpaperData wallpaper = getWallpaperSafeLocked(userId, 1);
                    if (padding.left < 0 || padding.top < 0 || padding.right < 0 || padding.bottom < 0) {
                        throw new IllegalArgumentException("padding must be positive: " + padding);
                    }
                    DisplayData wpdData = getDisplayDataOrCreate(displayId);
                    if (!padding.equals(wpdData.mPadding)) {
                        wpdData.mPadding.set(padding);
                        if (displayId == 0) {
                            saveSettingsLocked(userId);
                        }
                        if (this.mCurrentUserId == userId) {
                            if (wallpaper.connection != null) {
                                WallpaperConnection.DisplayConnector connector = wallpaper.connection.getDisplayConnectorOrCreate(displayId);
                                IWallpaperEngine engine = connector != null ? connector.mEngine : null;
                                if (engine != null) {
                                    try {
                                        engine.setDisplayPadding(padding);
                                    } catch (RemoteException e) {
                                    }
                                    notifyCallbacksLocked(wallpaper, 1);
                                } else if (!(wallpaper.connection.mService == null || connector == null)) {
                                    connector.mPaddingChanged = true;
                                }
                            }
                        } else {
                            return;
                        }
                    }
                    return;
                }
                throw new IllegalArgumentException("Cannot find display with id=" + displayId);
            }
        }
    }

    public ParcelFileDescriptor getWallpaper(String callingPkg, IWallpaperManagerCallback cb, int which, Bundle outParams, int wallpaperUserId) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.READ_WALLPAPER_INTERNAL") != 0) {
            ((StorageManager) this.mContext.getSystemService(StorageManager.class)).checkPermissionReadImages(true, Binder.getCallingPid(), Binder.getCallingUid(), callingPkg);
        }
        int wallpaperUserId2 = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), wallpaperUserId, false, true, "getWallpaper", null);
        if (which == 1 || which == 2) {
            synchronized (this.mLock) {
                WallpaperData wallpaper = (which == 2 ? this.mLockWallpaperMap : this.mWallpaperMap).get(wallpaperUserId2);
                if (wallpaper == null) {
                    return null;
                }
                DisplayData wpdData = getDisplayDataOrCreate(0);
                if (outParams != null) {
                    try {
                        outParams.putInt("width", wpdData.mWidth);
                        outParams.putInt("height", wpdData.mHeight);
                    } catch (FileNotFoundException e) {
                        Slog.w(TAG, "Error getting wallpaper", e);
                        return null;
                    } catch (NullPointerException e2) {
                        Slog.w(TAG, "getting wallpaper null", e2);
                        return null;
                    }
                }
                if (cb != null && !wallpaper.callbacks.isContainIBinder(cb)) {
                    wallpaper.callbacks.register(cb);
                }
                if (!wallpaper.cropFile.exists()) {
                    return null;
                }
                return ParcelFileDescriptor.open(wallpaper.cropFile, 268435456);
            }
        }
        throw new IllegalArgumentException("Must specify exactly one kind of wallpaper to read");
    }

    @Override // com.android.server.wallpaper.IHwWallpaperManagerInner
    public WallpaperInfo getWallpaperInfo(int userId) {
        int userId2 = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, true, "getWallpaperInfo", null);
        synchronized (this.mLock) {
            WallpaperData wallpaper = this.mWallpaperMap.get(userId2);
            if (wallpaper == null || wallpaper.connection == null) {
                return null;
            }
            return wallpaper.connection.mInfo;
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
                return wallpaper.wallpaperId;
            }
        }
        throw new IllegalArgumentException("Must specify exactly one kind of wallpaper");
    }

    public void registerWallpaperColorsCallback(IWallpaperManagerCallback cb, int userId, int displayId) {
        int userId2 = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, true, true, "registerWallpaperColorsCallback", null);
        synchronized (this.mLock) {
            SparseArray<RemoteCallbackList<IWallpaperManagerCallback>> userDisplayColorsChangedListeners = this.mColorsChangedListeners.get(userId2);
            if (userDisplayColorsChangedListeners == null) {
                userDisplayColorsChangedListeners = new SparseArray<>();
                this.mColorsChangedListeners.put(userId2, userDisplayColorsChangedListeners);
            }
            RemoteCallbackList<IWallpaperManagerCallback> displayChangedListeners = userDisplayColorsChangedListeners.get(displayId);
            if (displayChangedListeners == null) {
                displayChangedListeners = new RemoteCallbackList<>();
                userDisplayColorsChangedListeners.put(displayId, displayChangedListeners);
            }
            displayChangedListeners.register(cb);
        }
    }

    public void unregisterWallpaperColorsCallback(IWallpaperManagerCallback cb, int userId, int displayId) {
        RemoteCallbackList<IWallpaperManagerCallback> displayChangedListeners;
        int userId2 = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, true, true, "unregisterWallpaperColorsCallback", null);
        synchronized (this.mLock) {
            SparseArray<RemoteCallbackList<IWallpaperManagerCallback>> userDisplayColorsChangedListeners = this.mColorsChangedListeners.get(userId2);
            if (!(userDisplayColorsChangedListeners == null || (displayChangedListeners = userDisplayColorsChangedListeners.get(displayId)) == null)) {
                displayChangedListeners.unregister(cb);
            }
        }
    }

    public void setInAmbientMode(boolean inAmbientMode, long animationDuration) {
        IWallpaperEngine engine;
        synchronized (this.mLock) {
            this.mInAmbientMode = inAmbientMode;
            WallpaperData data = this.mWallpaperMap.get(this.mCurrentUserId);
            if (data == null || data.connection == null || (data.connection.mInfo != null && !data.connection.mInfo.supportsAmbientMode())) {
                engine = null;
            } else {
                engine = data.connection.getDisplayConnectorOrCreate(0).mEngine;
            }
        }
        if (engine != null) {
            try {
                engine.setInAmbientMode(inAmbientMode, animationDuration);
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

    public WallpaperColors getWallpaperColors(int which, int userId, int displayId) throws RemoteException {
        WallpaperColors wallpaperColors;
        boolean shouldExtract = true;
        if (which == 2 || which == 1) {
            int userId2 = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, true, "getWallpaperColors", null);
            WallpaperData wallpaperData = null;
            synchronized (this.mLock) {
                if (which == 2) {
                    try {
                        wallpaperData = this.mLockWallpaperMap.get(userId2);
                    } catch (Throwable th) {
                        throw th;
                    }
                }
                if (wallpaperData == null) {
                    wallpaperData = findWallpaperAtDisplay(userId2, displayId);
                }
                if (wallpaperData == null) {
                    return null;
                }
                if (wallpaperData.primaryColors != null) {
                    shouldExtract = false;
                }
            }
            if (shouldExtract) {
                extractColors(wallpaperData);
            }
            synchronized (this.mLock) {
                wallpaperColors = wallpaperData.primaryColors;
            }
            return wallpaperColors;
        }
        throw new IllegalArgumentException("which should be either FLAG_LOCK or FLAG_SYSTEM");
    }

    private WallpaperData findWallpaperAtDisplay(int userId, int displayId) {
        WallpaperData wallpaperData = this.mFallbackWallpaper;
        if (wallpaperData == null || wallpaperData.connection == null || !this.mFallbackWallpaper.connection.containsDisplay(displayId)) {
            return this.mWallpaperMap.get(userId);
        }
        return this.mFallbackWallpaper;
    }

    public ParcelFileDescriptor setWallpaper(String name, String callingPackage, Rect cropHint, boolean allowBackup, Bundle extras, int which, IWallpaperManagerCallback completion, int userId) {
        Rect cropHint2;
        Throwable th;
        Throwable th2;
        if (callingPackage != null && !callingPackage.equals("com.huawei.android.thememanager")) {
            long ident = Binder.clearCallingIdentity();
            try {
                try {
                    Settings.Secure.putStringForUser(this.mContext.getContentResolver(), "use_dark_wallpaper", "systemui", userId);
                    Binder.restoreCallingIdentity(ident);
                } catch (Throwable th3) {
                    th2 = th3;
                    Binder.restoreCallingIdentity(ident);
                    throw th2;
                }
            } catch (Throwable th4) {
                th2 = th4;
                Binder.restoreCallingIdentity(ident);
                throw th2;
            }
        }
        HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.WALLPAPERMANAGER_SETWALLPAPER);
        if (isMdmDisableChangeWallpaper()) {
            return null;
        }
        int userId2 = ActivityManager.handleIncomingUser(getCallingPid(), getCallingUid(), userId, false, true, "changing wallpaper", null);
        checkPermission("android.permission.SET_WALLPAPER");
        if ((which & 3) != 0) {
            if (isWallpaperSupported(callingPackage)) {
                if (isSetWallpaperAllowed(callingPackage)) {
                    if (cropHint == null) {
                        cropHint2 = new Rect(0, 0, 0, 0);
                    } else if (cropHint.isEmpty() || cropHint.left < 0 || cropHint.top < 0) {
                        throw new IllegalArgumentException("Invalid crop rect supplied: " + cropHint);
                    } else {
                        cropHint2 = cropHint;
                    }
                    synchronized (this.mLock) {
                        if ((which & 3) == 0) {
                            try {
                                if (this.mLockWallpaperMap.get(userId2) == null) {
                                    migrateSystemToLockWallpaperLocked(userId2);
                                }
                            } catch (Throwable th5) {
                                th = th5;
                                throw th;
                            }
                        }
                        WallpaperData wallpaper = getWallpaperSafeLocked(userId2, which);
                        long ident2 = Binder.clearCallingIdentity();
                        try {
                            ParcelFileDescriptor pfd = updateWallpaperBitmapLocked(name, wallpaper, extras);
                            if (pfd != null) {
                                wallpaper.imageWallpaperPending = true;
                                wallpaper.whichPending = which;
                                try {
                                    wallpaper.setComplete = completion;
                                    wallpaper.cropHint.set(cropHint2);
                                    try {
                                        wallpaper.allowBackup = allowBackup;
                                    } catch (Throwable th6) {
                                        th = th6;
                                    }
                                } catch (Throwable th7) {
                                    th = th7;
                                    Binder.restoreCallingIdentity(ident2);
                                    throw th;
                                }
                            }
                            Binder.restoreCallingIdentity(ident2);
                            return pfd;
                        } catch (Throwable th8) {
                            th = th8;
                            throw th;
                        }
                    }
                }
            }
            return null;
        }
        Slog.e(TAG, "Must specify a valid wallpaper category to set");
        throw new IllegalArgumentException("Must specify a valid wallpaper category to set");
    }

    private void migrateSystemToLockWallpaperLocked(int userId) {
        WallpaperData sysWP = this.mWallpaperMap.get(userId);
        if (sysWP != null) {
            WallpaperData lockWP = new WallpaperData(userId, WALLPAPER_LOCK_ORIG, WALLPAPER_LOCK_CROP);
            lockWP.wallpaperId = sysWP.wallpaperId;
            lockWP.cropHint.set(sysWP.cropHint);
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
                        this.mIHwWMEx.handleWallpaperObserverEvent(wallpaper);
                        if (bindWallpaperComponentLocked(name, false, true, wallpaper, null)) {
                            if (!same) {
                                wallpaper.primaryColors = null;
                            }
                            wallpaper.nextWallpaperComponent = wallpaper.wallpaperComponent;
                            wallpaper.wallpaperId = makeWallpaperIdLocked();
                            notifyCallbacksLocked(wallpaper, 1);
                            shouldNotifyColors = true;
                        }
                    } finally {
                        Binder.restoreCallingIdentity(ident);
                    }
                } else {
                    throw new IllegalStateException("Wallpaper not yet initialized for user " + userId2);
                }
            }
            if (shouldNotifyColors) {
                notifyWallpaperColorsChanged(wallpaper, which);
                notifyWallpaperColorsChanged(this.mFallbackWallpaper, 1);
            }
        }
    }

    private boolean changingToSame(ComponentName componentName, WallpaperData wallpaper) {
        if (wallpaper.connection == null) {
            return false;
        }
        if (wallpaper.wallpaperComponent == null) {
            if (componentName == null) {
                return true;
            }
            return false;
        } else if (wallpaper.wallpaperComponent.equals(componentName)) {
            return true;
        } else {
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:102:0x022a  */
    /* JADX WARNING: Removed duplicated region for block: B:104:0x022f  */
    public boolean bindWallpaperComponentLocked(ComponentName componentName, boolean force, boolean fromUser, WallpaperData wallpaper, IRemoteCallback reply) {
        RemoteException e;
        ComponentName componentName2 = componentName;
        Slog.v(TAG, "bindWallpaperComponentLocked: componentName=" + componentName2);
        if (force || !changingToSame(componentName2, wallpaper)) {
            if (componentName2 == null) {
                try {
                    componentName2 = this.mDefaultWallpaperComponent;
                    if (componentName2 == null) {
                        try {
                            componentName2 = this.mImageWallpaper;
                            Slog.v(TAG, "No default component; using image wallpaper");
                        } catch (RemoteException e2) {
                            e = e2;
                            String msg = "Remote exception for " + componentName2 + "\n" + e;
                            if (!fromUser) {
                            }
                        }
                    }
                } catch (RemoteException e3) {
                    e = e3;
                    String msg2 = "Remote exception for " + componentName2 + "\n" + e;
                    if (!fromUser) {
                    }
                }
            }
            int serviceUserId = wallpaper.userId;
            ServiceInfo si = this.mIPackageManager.getServiceInfo(componentName2, 4224, serviceUserId);
            if (si == null) {
                Slog.w(TAG, "Attempted wallpaper " + componentName2 + " is unavailable");
                this.mIsLoadLiveWallpaper = false;
                return false;
            } else if (!"android.permission.BIND_WALLPAPER".equals(si.permission)) {
                String msg3 = "Selected service does not have android.permission.BIND_WALLPAPER: " + componentName2;
                if (!fromUser) {
                    Slog.w(TAG, msg3);
                    return false;
                }
                throw new SecurityException(msg3);
            } else {
                WallpaperInfo wi = null;
                Intent intent = new Intent("android.service.wallpaper.WallpaperService");
                if (componentName2 != null && !componentName2.equals(this.mImageWallpaper)) {
                    List<ResolveInfo> ris = this.mIPackageManager.queryIntentServices(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), 128, serviceUserId).getList();
                    int i = 0;
                    while (true) {
                        if (i >= ris.size()) {
                            break;
                        }
                        ServiceInfo rsi = ris.get(i).serviceInfo;
                        if (!rsi.name.equals(si.name) || !rsi.packageName.equals(si.packageName)) {
                            i++;
                        } else {
                            try {
                                wi = new WallpaperInfo(this.mContext, ris.get(i));
                                break;
                            } catch (XmlPullParserException e4) {
                                if (!fromUser) {
                                    Slog.w(TAG, e4);
                                    return false;
                                }
                                throw new IllegalArgumentException(e4);
                            } catch (IOException e5) {
                                if (!fromUser) {
                                    Slog.w(TAG, e5);
                                    return false;
                                }
                                throw new IllegalArgumentException(e5);
                            }
                        }
                    }
                    if (wi == null) {
                        String msg4 = "Selected service is not a wallpaper: " + componentName2;
                        if (!fromUser) {
                            Slog.w(TAG, msg4);
                            return false;
                        }
                        throw new SecurityException(msg4);
                    }
                }
                if (wi == null || !wi.supportsAmbientMode() || this.mIPackageManager.checkPermission("android.permission.AMBIENT_WALLPAPER", wi.getPackageName(), serviceUserId) == 0) {
                    WallpaperConnection newConn = new WallpaperConnection(wi, wallpaper, this.mIPackageManager.getPackageUid(componentName2.getPackageName(), 268435456, wallpaper.userId));
                    intent.setComponent(componentName2);
                    intent.putExtra("android.intent.extra.client_label", 17041483);
                    intent.putExtra("android.intent.extra.client_intent", PendingIntent.getActivityAsUser(this.mContext, 0, Intent.createChooser(new Intent("android.intent.action.SET_WALLPAPER"), this.mContext.getText(17039774)), 0, null, new UserHandle(serviceUserId)));
                    if (!this.mContext.bindServiceAsUser(intent, newConn, 570429441, new UserHandle(serviceUserId))) {
                        String msg5 = "Unable to bind service: " + componentName2;
                        if (!fromUser) {
                            Slog.w(TAG, msg5);
                            return false;
                        }
                        throw new IllegalArgumentException(msg5);
                    }
                    this.mIHwWMEx.reportWallpaper(componentName2);
                    if (wallpaper.userId == this.mCurrentUserId && this.mLastWallpaper != null && !wallpaper.equals(this.mFallbackWallpaper)) {
                        detachWallpaperLocked(this.mLastWallpaper);
                    }
                    wallpaper.wallpaperComponent = componentName2;
                    wallpaper.connection = newConn;
                    try {
                        newConn.mReply = reply;
                        if (wallpaper.userId == this.mCurrentUserId && !wallpaper.equals(this.mFallbackWallpaper)) {
                            this.mLastWallpaper = wallpaper;
                        }
                        updateFallbackConnection();
                        return true;
                    } catch (RemoteException e6) {
                        e = e6;
                        String msg22 = "Remote exception for " + componentName2 + "\n" + e;
                        if (!fromUser) {
                            Slog.w(TAG, msg22);
                            return false;
                        }
                        throw new IllegalArgumentException(msg22);
                    }
                } else {
                    String msg6 = "Selected service does not have android.permission.AMBIENT_WALLPAPER: " + componentName2;
                    if (!fromUser) {
                        Slog.w(TAG, msg6);
                        return false;
                    }
                    throw new SecurityException(msg6);
                }
            }
        } else {
            Slog.v(TAG, "bindWallpaperComponentLocked: component is not changed");
            return true;
        }
    }

    private void detachWallpaperLocked(WallpaperData wallpaper) {
        if (wallpaper.connection != null) {
            if (wallpaper.connection.mReply != null) {
                try {
                    wallpaper.connection.mReply.sendResult((Bundle) null);
                } catch (RemoteException e) {
                }
                wallpaper.connection.mReply = null;
            }
            try {
                if (wallpaper.connection.mService != null) {
                    wallpaper.connection.mService.detach();
                }
            } catch (RemoteException e2) {
                Slog.w(TAG, "Failed detaching wallpaper service ", e2);
            }
            this.mContext.unbindService(wallpaper.connection);
            wallpaper.connection.forEachDisplayConnector($$Lambda$havGP5uMdRgWQrLydPeIOu1qDGE.INSTANCE);
            wallpaper.connection.mService = null;
            wallpaper.connection.mDisplayConnector.clear();
            wallpaper.connection = null;
            if (wallpaper == this.mLastWallpaper) {
                this.mLastWallpaper = null;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearWallpaperComponentLocked(WallpaperData wallpaper) {
        wallpaper.wallpaperComponent = null;
        detachWallpaperLocked(wallpaper);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void attachServiceLocked(WallpaperConnection conn, WallpaperData wallpaper) {
        conn.forEachDisplayConnector(new Consumer(wallpaper) {
            /* class com.android.server.wallpaper.$$Lambda$WallpaperManagerService$UhAlBGB5jhuZrLndUPRmIvoHRZc */
            private final /* synthetic */ WallpaperManagerService.WallpaperData f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ((WallpaperManagerService.WallpaperConnection.DisplayConnector) obj).connectLocked(WallpaperManagerService.WallpaperConnection.this, this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyCallbacksLocked(WallpaperData wallpaper, int wallPaperFlag) {
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
        Intent intent = new Intent("android.intent.action.WALLPAPER_CHANGED");
        if ((wallPaperFlag & 2) == 2) {
            intent.putExtra("WallPaperType", "lock");
        }
        this.mContext.sendBroadcastAsUser(intent, new UserHandle(this.mCurrentUserId));
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDisplayReadyInternal(int displayId) {
        synchronized (this.mLock) {
            if (this.mLastWallpaper != null) {
                if (supportsMultiDisplay(this.mLastWallpaper.connection)) {
                    WallpaperConnection.DisplayConnector connector = this.mLastWallpaper.connection.getDisplayConnectorOrCreate(displayId);
                    if (connector != null) {
                        connector.connectLocked(this.mLastWallpaper.connection, this.mLastWallpaper);
                        return;
                    }
                    return;
                }
                if (this.mFallbackWallpaper != null) {
                    WallpaperConnection.DisplayConnector connector2 = this.mFallbackWallpaper.connection.getDisplayConnectorOrCreate(displayId);
                    if (connector2 != null) {
                        connector2.connectLocked(this.mFallbackWallpaper.connection, this.mFallbackWallpaper);
                    }
                } else {
                    Slog.w(TAG, "No wallpaper can be added to the new display");
                }
            }
        }
    }

    private static JournaledFile makeJournaledFile(int userId) {
        String base = new File(getWallpaperDir(userId), WALLPAPER_INFO).getAbsolutePath();
        File file = new File(base);
        return new JournaledFile(file, new File(base + ".tmp"));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void saveSettingsLocked(int userId) {
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
            IoUtils.closeQuietly((AutoCloseable) null);
            journal.rollback();
        }
    }

    private void writeWallpaperAttributes(XmlSerializer out, String tag, WallpaperData wallpaper) throws IllegalArgumentException, IllegalStateException, IOException {
        DisplayData wpdData = getDisplayDataOrCreate(0);
        out.startTag(null, tag);
        out.attribute(null, "id", Integer.toString(wallpaper.wallpaperId));
        out.attribute(null, "width", Integer.toString(wpdData.mWidth));
        out.attribute(null, "height", Integer.toString(wpdData.mHeight));
        out.attribute(null, "cropLeft", Integer.toString(wallpaper.cropHint.left));
        out.attribute(null, "cropTop", Integer.toString(wallpaper.cropHint.top));
        out.attribute(null, "cropRight", Integer.toString(wallpaper.cropHint.right));
        out.attribute(null, "cropBottom", Integer.toString(wallpaper.cropHint.bottom));
        if (wpdData.mPadding.left != 0) {
            out.attribute(null, "paddingLeft", Integer.toString(wpdData.mPadding.left));
        }
        if (wpdData.mPadding.top != 0) {
            out.attribute(null, "paddingTop", Integer.toString(wpdData.mPadding.top));
        }
        if (wpdData.mPadding.right != 0) {
            out.attribute(null, "paddingRight", Integer.toString(wpdData.mPadding.right));
        }
        if (wpdData.mPadding.bottom != 0) {
            out.attribute(null, "paddingBottom", Integer.toString(wpdData.mPadding.bottom));
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
            ensureSaneWallpaperData(wallpaper3, 0);
            return wallpaper3;
        }
        Slog.wtf(TAG, "Didn't find wallpaper in non-lock case!");
        WallpaperData wallpaper4 = new WallpaperData(userId, WALLPAPER, WALLPAPER_CROP);
        this.mWallpaperMap.put(userId, wallpaper4);
        ensureSaneWallpaperData(wallpaper4, 0);
        return wallpaper4;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:130:0x0256  */
    /* JADX WARNING: Removed duplicated region for block: B:131:0x026b  */
    /* JADX WARNING: Removed duplicated region for block: B:136:0x0286  */
    /* JADX WARNING: Removed duplicated region for block: B:139:? A[RETURN, SYNTHETIC] */
    private void loadSettingsLocked(int userId, boolean keepDimensionHints) {
        WallpaperData wallpaper;
        WallpaperData lockWallpaper;
        NullPointerException e;
        NumberFormatException e2;
        XmlPullParserException e3;
        IOException e4;
        IndexOutOfBoundsException e5;
        FileInputStream stream;
        JournaledFile journal;
        JournaledFile journal2 = makeJournaledFile(userId);
        FileInputStream stream2 = null;
        File file = journal2.chooseForRead();
        WallpaperData wallpaper2 = this.mWallpaperMap.get(userId);
        if (wallpaper2 == null) {
            migrateFromOld();
            WallpaperData wallpaper3 = new WallpaperData(userId, WALLPAPER, WALLPAPER_CROP);
            wallpaper3.allowBackup = true;
            this.mWallpaperMap.put(userId, wallpaper3);
            if (!wallpaper3.cropExists()) {
                if (wallpaper3.sourceExists()) {
                    generateCrop(wallpaper3);
                } else {
                    Slog.i(TAG, "No static wallpaper imagery; defaults will be shown");
                }
            }
            initializeFallbackWallpaper();
            wallpaper = wallpaper3;
        } else {
            wallpaper = wallpaper2;
        }
        boolean success = false;
        DisplayData wpdData = getDisplayDataOrCreate(0);
        try {
            stream2 = new FileInputStream(file);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream2, StandardCharsets.UTF_8.name());
                while (true) {
                    int type = parser.next();
                    if (type == 2) {
                        String tag = parser.getName();
                        if ("wp".equals(tag)) {
                            try {
                                parseWallpaperAttributes(parser, wallpaper, keepDimensionHints);
                                journal = journal2;
                                JournaledFile journal3 = null;
                                try {
                                    String comp = parser.getAttributeValue(null, "component");
                                    if (comp != null) {
                                        try {
                                            journal3 = ComponentName.unflattenFromString(comp);
                                        } catch (FileNotFoundException e6) {
                                        } catch (NullPointerException e7) {
                                            e = e7;
                                            Slog.w(TAG, "failed parsing " + file + " " + e);
                                            IoUtils.closeQuietly(stream2);
                                            this.mSuccess = success;
                                            if (success) {
                                            }
                                            ensureSaneWallpaperDisplaySize(wpdData, 0);
                                            ensureSaneWallpaperData(wallpaper, 0);
                                            lockWallpaper = this.mLockWallpaperMap.get(userId);
                                            if (lockWallpaper == null) {
                                            }
                                        } catch (NumberFormatException e8) {
                                            e2 = e8;
                                            Slog.w(TAG, "failed parsing " + file + " " + e2);
                                            IoUtils.closeQuietly(stream2);
                                            this.mSuccess = success;
                                            if (success) {
                                            }
                                            ensureSaneWallpaperDisplaySize(wpdData, 0);
                                            ensureSaneWallpaperData(wallpaper, 0);
                                            lockWallpaper = this.mLockWallpaperMap.get(userId);
                                            if (lockWallpaper == null) {
                                            }
                                        } catch (XmlPullParserException e9) {
                                            e3 = e9;
                                            Slog.w(TAG, "failed parsing " + file + " " + e3);
                                            IoUtils.closeQuietly(stream2);
                                            this.mSuccess = success;
                                            if (success) {
                                            }
                                            ensureSaneWallpaperDisplaySize(wpdData, 0);
                                            ensureSaneWallpaperData(wallpaper, 0);
                                            lockWallpaper = this.mLockWallpaperMap.get(userId);
                                            if (lockWallpaper == null) {
                                            }
                                        } catch (IOException e10) {
                                            e4 = e10;
                                            Slog.w(TAG, "failed parsing " + file + " " + e4);
                                            IoUtils.closeQuietly(stream2);
                                            this.mSuccess = success;
                                            if (success) {
                                            }
                                            ensureSaneWallpaperDisplaySize(wpdData, 0);
                                            ensureSaneWallpaperData(wallpaper, 0);
                                            lockWallpaper = this.mLockWallpaperMap.get(userId);
                                            if (lockWallpaper == null) {
                                            }
                                        } catch (IndexOutOfBoundsException e11) {
                                            e5 = e11;
                                            Slog.w(TAG, "failed parsing " + file + " " + e5);
                                            IoUtils.closeQuietly(stream2);
                                            this.mSuccess = success;
                                            if (success) {
                                            }
                                            ensureSaneWallpaperDisplaySize(wpdData, 0);
                                            ensureSaneWallpaperData(wallpaper, 0);
                                            lockWallpaper = this.mLockWallpaperMap.get(userId);
                                            if (lockWallpaper == null) {
                                            }
                                        }
                                    }
                                    wallpaper.nextWallpaperComponent = journal3;
                                    if (wallpaper.nextWallpaperComponent != null) {
                                        stream = stream2;
                                        try {
                                            if (PackageManagerService.PLATFORM_PACKAGE_NAME.equals(wallpaper.nextWallpaperComponent.getPackageName())) {
                                            }
                                        } catch (FileNotFoundException e12) {
                                            stream2 = stream;
                                            Slog.w(TAG, "no current wallpaper -- first boot?");
                                            this.mDefaultWallpaperComponent = WallpaperManager.getDefaultWallpaperComponent(userId);
                                            Slog.w(TAG, "first boot set mDefaultWallpaperComponent=" + this.mDefaultWallpaperComponent);
                                            IoUtils.closeQuietly(stream2);
                                            this.mSuccess = success;
                                            if (success) {
                                            }
                                            ensureSaneWallpaperDisplaySize(wpdData, 0);
                                            ensureSaneWallpaperData(wallpaper, 0);
                                            lockWallpaper = this.mLockWallpaperMap.get(userId);
                                            if (lockWallpaper == null) {
                                            }
                                        } catch (NullPointerException e13) {
                                            e = e13;
                                            stream2 = stream;
                                            Slog.w(TAG, "failed parsing " + file + " " + e);
                                            IoUtils.closeQuietly(stream2);
                                            this.mSuccess = success;
                                            if (success) {
                                            }
                                            ensureSaneWallpaperDisplaySize(wpdData, 0);
                                            ensureSaneWallpaperData(wallpaper, 0);
                                            lockWallpaper = this.mLockWallpaperMap.get(userId);
                                            if (lockWallpaper == null) {
                                            }
                                        } catch (NumberFormatException e14) {
                                            e2 = e14;
                                            stream2 = stream;
                                            Slog.w(TAG, "failed parsing " + file + " " + e2);
                                            IoUtils.closeQuietly(stream2);
                                            this.mSuccess = success;
                                            if (success) {
                                            }
                                            ensureSaneWallpaperDisplaySize(wpdData, 0);
                                            ensureSaneWallpaperData(wallpaper, 0);
                                            lockWallpaper = this.mLockWallpaperMap.get(userId);
                                            if (lockWallpaper == null) {
                                            }
                                        } catch (XmlPullParserException e15) {
                                            e3 = e15;
                                            stream2 = stream;
                                            Slog.w(TAG, "failed parsing " + file + " " + e3);
                                            IoUtils.closeQuietly(stream2);
                                            this.mSuccess = success;
                                            if (success) {
                                            }
                                            ensureSaneWallpaperDisplaySize(wpdData, 0);
                                            ensureSaneWallpaperData(wallpaper, 0);
                                            lockWallpaper = this.mLockWallpaperMap.get(userId);
                                            if (lockWallpaper == null) {
                                            }
                                        } catch (IOException e16) {
                                            e4 = e16;
                                            stream2 = stream;
                                            Slog.w(TAG, "failed parsing " + file + " " + e4);
                                            IoUtils.closeQuietly(stream2);
                                            this.mSuccess = success;
                                            if (success) {
                                            }
                                            ensureSaneWallpaperDisplaySize(wpdData, 0);
                                            ensureSaneWallpaperData(wallpaper, 0);
                                            lockWallpaper = this.mLockWallpaperMap.get(userId);
                                            if (lockWallpaper == null) {
                                            }
                                        } catch (IndexOutOfBoundsException e17) {
                                            e5 = e17;
                                            stream2 = stream;
                                            Slog.w(TAG, "failed parsing " + file + " " + e5);
                                            IoUtils.closeQuietly(stream2);
                                            this.mSuccess = success;
                                            if (success) {
                                            }
                                            ensureSaneWallpaperDisplaySize(wpdData, 0);
                                            ensureSaneWallpaperData(wallpaper, 0);
                                            lockWallpaper = this.mLockWallpaperMap.get(userId);
                                            if (lockWallpaper == null) {
                                            }
                                        }
                                    } else {
                                        stream = stream2;
                                    }
                                    wallpaper.nextWallpaperComponent = this.mImageWallpaper;
                                } catch (FileNotFoundException e18) {
                                    Slog.w(TAG, "no current wallpaper -- first boot?");
                                    this.mDefaultWallpaperComponent = WallpaperManager.getDefaultWallpaperComponent(userId);
                                    Slog.w(TAG, "first boot set mDefaultWallpaperComponent=" + this.mDefaultWallpaperComponent);
                                    IoUtils.closeQuietly(stream2);
                                    this.mSuccess = success;
                                    if (success) {
                                    }
                                    ensureSaneWallpaperDisplaySize(wpdData, 0);
                                    ensureSaneWallpaperData(wallpaper, 0);
                                    lockWallpaper = this.mLockWallpaperMap.get(userId);
                                    if (lockWallpaper == null) {
                                    }
                                } catch (NullPointerException e19) {
                                    e = e19;
                                    Slog.w(TAG, "failed parsing " + file + " " + e);
                                    IoUtils.closeQuietly(stream2);
                                    this.mSuccess = success;
                                    if (success) {
                                    }
                                    ensureSaneWallpaperDisplaySize(wpdData, 0);
                                    ensureSaneWallpaperData(wallpaper, 0);
                                    lockWallpaper = this.mLockWallpaperMap.get(userId);
                                    if (lockWallpaper == null) {
                                    }
                                } catch (NumberFormatException e20) {
                                    e2 = e20;
                                    Slog.w(TAG, "failed parsing " + file + " " + e2);
                                    IoUtils.closeQuietly(stream2);
                                    this.mSuccess = success;
                                    if (success) {
                                    }
                                    ensureSaneWallpaperDisplaySize(wpdData, 0);
                                    ensureSaneWallpaperData(wallpaper, 0);
                                    lockWallpaper = this.mLockWallpaperMap.get(userId);
                                    if (lockWallpaper == null) {
                                    }
                                } catch (XmlPullParserException e21) {
                                    e3 = e21;
                                    Slog.w(TAG, "failed parsing " + file + " " + e3);
                                    IoUtils.closeQuietly(stream2);
                                    this.mSuccess = success;
                                    if (success) {
                                    }
                                    ensureSaneWallpaperDisplaySize(wpdData, 0);
                                    ensureSaneWallpaperData(wallpaper, 0);
                                    lockWallpaper = this.mLockWallpaperMap.get(userId);
                                    if (lockWallpaper == null) {
                                    }
                                } catch (IOException e22) {
                                    e4 = e22;
                                    Slog.w(TAG, "failed parsing " + file + " " + e4);
                                    IoUtils.closeQuietly(stream2);
                                    this.mSuccess = success;
                                    if (success) {
                                    }
                                    ensureSaneWallpaperDisplaySize(wpdData, 0);
                                    ensureSaneWallpaperData(wallpaper, 0);
                                    lockWallpaper = this.mLockWallpaperMap.get(userId);
                                    if (lockWallpaper == null) {
                                    }
                                } catch (IndexOutOfBoundsException e23) {
                                    e5 = e23;
                                    Slog.w(TAG, "failed parsing " + file + " " + e5);
                                    IoUtils.closeQuietly(stream2);
                                    this.mSuccess = success;
                                    if (success) {
                                    }
                                    ensureSaneWallpaperDisplaySize(wpdData, 0);
                                    ensureSaneWallpaperData(wallpaper, 0);
                                    lockWallpaper = this.mLockWallpaperMap.get(userId);
                                    if (lockWallpaper == null) {
                                    }
                                }
                            } catch (FileNotFoundException e24) {
                                Slog.w(TAG, "no current wallpaper -- first boot?");
                                this.mDefaultWallpaperComponent = WallpaperManager.getDefaultWallpaperComponent(userId);
                                Slog.w(TAG, "first boot set mDefaultWallpaperComponent=" + this.mDefaultWallpaperComponent);
                                IoUtils.closeQuietly(stream2);
                                this.mSuccess = success;
                                if (success) {
                                }
                                ensureSaneWallpaperDisplaySize(wpdData, 0);
                                ensureSaneWallpaperData(wallpaper, 0);
                                lockWallpaper = this.mLockWallpaperMap.get(userId);
                                if (lockWallpaper == null) {
                                }
                            } catch (NullPointerException e25) {
                                e = e25;
                                Slog.w(TAG, "failed parsing " + file + " " + e);
                                IoUtils.closeQuietly(stream2);
                                this.mSuccess = success;
                                if (success) {
                                }
                                ensureSaneWallpaperDisplaySize(wpdData, 0);
                                ensureSaneWallpaperData(wallpaper, 0);
                                lockWallpaper = this.mLockWallpaperMap.get(userId);
                                if (lockWallpaper == null) {
                                }
                            } catch (NumberFormatException e26) {
                                e2 = e26;
                                Slog.w(TAG, "failed parsing " + file + " " + e2);
                                IoUtils.closeQuietly(stream2);
                                this.mSuccess = success;
                                if (success) {
                                }
                                ensureSaneWallpaperDisplaySize(wpdData, 0);
                                ensureSaneWallpaperData(wallpaper, 0);
                                lockWallpaper = this.mLockWallpaperMap.get(userId);
                                if (lockWallpaper == null) {
                                }
                            } catch (XmlPullParserException e27) {
                                e3 = e27;
                                Slog.w(TAG, "failed parsing " + file + " " + e3);
                                IoUtils.closeQuietly(stream2);
                                this.mSuccess = success;
                                if (success) {
                                }
                                ensureSaneWallpaperDisplaySize(wpdData, 0);
                                ensureSaneWallpaperData(wallpaper, 0);
                                lockWallpaper = this.mLockWallpaperMap.get(userId);
                                if (lockWallpaper == null) {
                                }
                            } catch (IOException e28) {
                                e4 = e28;
                                Slog.w(TAG, "failed parsing " + file + " " + e4);
                                IoUtils.closeQuietly(stream2);
                                this.mSuccess = success;
                                if (success) {
                                }
                                ensureSaneWallpaperDisplaySize(wpdData, 0);
                                ensureSaneWallpaperData(wallpaper, 0);
                                lockWallpaper = this.mLockWallpaperMap.get(userId);
                                if (lockWallpaper == null) {
                                }
                            } catch (IndexOutOfBoundsException e29) {
                                e5 = e29;
                                Slog.w(TAG, "failed parsing " + file + " " + e5);
                                IoUtils.closeQuietly(stream2);
                                this.mSuccess = success;
                                if (success) {
                                }
                                ensureSaneWallpaperDisplaySize(wpdData, 0);
                                ensureSaneWallpaperData(wallpaper, 0);
                                lockWallpaper = this.mLockWallpaperMap.get(userId);
                                if (lockWallpaper == null) {
                                }
                            }
                        } else {
                            journal = journal2;
                            stream = stream2;
                            if ("kwp".equals(tag)) {
                                WallpaperData lockWallpaper2 = this.mLockWallpaperMap.get(userId);
                                if (lockWallpaper2 == null) {
                                    lockWallpaper2 = new WallpaperData(userId, WALLPAPER_LOCK_ORIG, WALLPAPER_LOCK_CROP);
                                    this.mLockWallpaperMap.put(userId, lockWallpaper2);
                                }
                                parseWallpaperAttributes(parser, lockWallpaper2, false);
                            }
                        }
                    } else {
                        journal = journal2;
                        stream = stream2;
                    }
                    if (type == 1) {
                        break;
                    }
                    journal2 = journal;
                    stream2 = stream;
                }
                success = true;
                stream2 = stream;
            } catch (FileNotFoundException e30) {
                Slog.w(TAG, "no current wallpaper -- first boot?");
                this.mDefaultWallpaperComponent = WallpaperManager.getDefaultWallpaperComponent(userId);
                Slog.w(TAG, "first boot set mDefaultWallpaperComponent=" + this.mDefaultWallpaperComponent);
                IoUtils.closeQuietly(stream2);
                this.mSuccess = success;
                if (success) {
                }
                ensureSaneWallpaperDisplaySize(wpdData, 0);
                ensureSaneWallpaperData(wallpaper, 0);
                lockWallpaper = this.mLockWallpaperMap.get(userId);
                if (lockWallpaper == null) {
                }
            } catch (NullPointerException e31) {
                e = e31;
                Slog.w(TAG, "failed parsing " + file + " " + e);
                IoUtils.closeQuietly(stream2);
                this.mSuccess = success;
                if (success) {
                }
                ensureSaneWallpaperDisplaySize(wpdData, 0);
                ensureSaneWallpaperData(wallpaper, 0);
                lockWallpaper = this.mLockWallpaperMap.get(userId);
                if (lockWallpaper == null) {
                }
            } catch (NumberFormatException e32) {
                e2 = e32;
                Slog.w(TAG, "failed parsing " + file + " " + e2);
                IoUtils.closeQuietly(stream2);
                this.mSuccess = success;
                if (success) {
                }
                ensureSaneWallpaperDisplaySize(wpdData, 0);
                ensureSaneWallpaperData(wallpaper, 0);
                lockWallpaper = this.mLockWallpaperMap.get(userId);
                if (lockWallpaper == null) {
                }
            } catch (XmlPullParserException e33) {
                e3 = e33;
                Slog.w(TAG, "failed parsing " + file + " " + e3);
                IoUtils.closeQuietly(stream2);
                this.mSuccess = success;
                if (success) {
                }
                ensureSaneWallpaperDisplaySize(wpdData, 0);
                ensureSaneWallpaperData(wallpaper, 0);
                lockWallpaper = this.mLockWallpaperMap.get(userId);
                if (lockWallpaper == null) {
                }
            } catch (IOException e34) {
                e4 = e34;
                Slog.w(TAG, "failed parsing " + file + " " + e4);
                IoUtils.closeQuietly(stream2);
                this.mSuccess = success;
                if (success) {
                }
                ensureSaneWallpaperDisplaySize(wpdData, 0);
                ensureSaneWallpaperData(wallpaper, 0);
                lockWallpaper = this.mLockWallpaperMap.get(userId);
                if (lockWallpaper == null) {
                }
            } catch (IndexOutOfBoundsException e35) {
                e5 = e35;
                Slog.w(TAG, "failed parsing " + file + " " + e5);
                IoUtils.closeQuietly(stream2);
                this.mSuccess = success;
                if (success) {
                }
                ensureSaneWallpaperDisplaySize(wpdData, 0);
                ensureSaneWallpaperData(wallpaper, 0);
                lockWallpaper = this.mLockWallpaperMap.get(userId);
                if (lockWallpaper == null) {
                }
            }
        } catch (FileNotFoundException e36) {
            Slog.w(TAG, "no current wallpaper -- first boot?");
            this.mDefaultWallpaperComponent = WallpaperManager.getDefaultWallpaperComponent(userId);
            Slog.w(TAG, "first boot set mDefaultWallpaperComponent=" + this.mDefaultWallpaperComponent);
            IoUtils.closeQuietly(stream2);
            this.mSuccess = success;
            if (success) {
            }
            ensureSaneWallpaperDisplaySize(wpdData, 0);
            ensureSaneWallpaperData(wallpaper, 0);
            lockWallpaper = this.mLockWallpaperMap.get(userId);
            if (lockWallpaper == null) {
            }
        } catch (NullPointerException e37) {
            e = e37;
            Slog.w(TAG, "failed parsing " + file + " " + e);
            IoUtils.closeQuietly(stream2);
            this.mSuccess = success;
            if (success) {
            }
            ensureSaneWallpaperDisplaySize(wpdData, 0);
            ensureSaneWallpaperData(wallpaper, 0);
            lockWallpaper = this.mLockWallpaperMap.get(userId);
            if (lockWallpaper == null) {
            }
        } catch (NumberFormatException e38) {
            e2 = e38;
            Slog.w(TAG, "failed parsing " + file + " " + e2);
            IoUtils.closeQuietly(stream2);
            this.mSuccess = success;
            if (success) {
            }
            ensureSaneWallpaperDisplaySize(wpdData, 0);
            ensureSaneWallpaperData(wallpaper, 0);
            lockWallpaper = this.mLockWallpaperMap.get(userId);
            if (lockWallpaper == null) {
            }
        } catch (XmlPullParserException e39) {
            e3 = e39;
            Slog.w(TAG, "failed parsing " + file + " " + e3);
            IoUtils.closeQuietly(stream2);
            this.mSuccess = success;
            if (success) {
            }
            ensureSaneWallpaperDisplaySize(wpdData, 0);
            ensureSaneWallpaperData(wallpaper, 0);
            lockWallpaper = this.mLockWallpaperMap.get(userId);
            if (lockWallpaper == null) {
            }
        } catch (IOException e40) {
            e4 = e40;
            Slog.w(TAG, "failed parsing " + file + " " + e4);
            IoUtils.closeQuietly(stream2);
            this.mSuccess = success;
            if (success) {
            }
            ensureSaneWallpaperDisplaySize(wpdData, 0);
            ensureSaneWallpaperData(wallpaper, 0);
            lockWallpaper = this.mLockWallpaperMap.get(userId);
            if (lockWallpaper == null) {
            }
        } catch (IndexOutOfBoundsException e41) {
            e5 = e41;
            Slog.w(TAG, "failed parsing " + file + " " + e5);
            IoUtils.closeQuietly(stream2);
            this.mSuccess = success;
            if (success) {
            }
            ensureSaneWallpaperDisplaySize(wpdData, 0);
            ensureSaneWallpaperData(wallpaper, 0);
            lockWallpaper = this.mLockWallpaperMap.get(userId);
            if (lockWallpaper == null) {
            }
        }
        IoUtils.closeQuietly(stream2);
        this.mSuccess = success;
        if (success) {
            wallpaper.cropHint.set(0, 0, 0, 0);
            wpdData.mPadding.set(0, 0, 0, 0);
            wallpaper.name = "";
            this.mLockWallpaperMap.remove(userId);
        } else if (wallpaper.wallpaperId <= 0) {
            wallpaper.wallpaperId = makeWallpaperIdLocked();
        }
        ensureSaneWallpaperDisplaySize(wpdData, 0);
        ensureSaneWallpaperData(wallpaper, 0);
        lockWallpaper = this.mLockWallpaperMap.get(userId);
        if (lockWallpaper == null) {
            ensureSaneWallpaperData(lockWallpaper, 0);
        }
    }

    private void initializeFallbackWallpaper() {
        if (this.mFallbackWallpaper == null) {
            this.mFallbackWallpaper = new WallpaperData(0, WALLPAPER, WALLPAPER_CROP);
            WallpaperData wallpaperData = this.mFallbackWallpaper;
            wallpaperData.allowBackup = false;
            wallpaperData.wallpaperId = makeWallpaperIdLocked();
            bindWallpaperComponentLocked(this.mImageWallpaper, true, false, this.mFallbackWallpaper, null);
        }
    }

    private void ensureSaneWallpaperData(WallpaperData wallpaper, int displayId) {
        DisplayData size = getDisplayDataOrCreate(displayId);
        if (displayId != 0) {
            return;
        }
        if (wallpaper.cropHint.width() <= 0 || wallpaper.cropHint.height() <= 0) {
            wallpaper.cropHint.set(0, 0, size.mWidth, size.mHeight);
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
        DisplayData wpData = getDisplayDataOrCreate(0);
        if (!keepDimensionHints) {
            wpData.mWidth = Integer.parseInt(parser.getAttributeValue(null, "width"));
            wpData.mHeight = Integer.parseInt(parser.getAttributeValue(null, "height"));
        }
        wallpaper.cropHint.left = getAttributeInt(parser, "cropLeft", 0);
        wallpaper.cropHint.top = getAttributeInt(parser, "cropTop", 0);
        wallpaper.cropHint.right = getAttributeInt(parser, "cropRight", 0);
        wallpaper.cropHint.bottom = getAttributeInt(parser, "cropBottom", 0);
        wpData.mPadding.left = getAttributeInt(parser, "paddingLeft", 0);
        wpData.mPadding.top = getAttributeInt(parser, "paddingTop", 0);
        wpData.mPadding.right = getAttributeInt(parser, "paddingRight", 0);
        wpData.mPadding.bottom = getAttributeInt(parser, "paddingBottom", 0);
        int colorsCount = getAttributeInt(parser, "colorsCount", 0);
        if (colorsCount > 0) {
            Color primary = null;
            Color secondary = null;
            Color tertiary = null;
            for (int i = 0; i < colorsCount; i++) {
                Color color = Color.valueOf(getAttributeInt(parser, "colorValue" + i, 0));
                if (i == 0) {
                    primary = color;
                } else if (i != 1) {
                    if (i != 2) {
                        break;
                    }
                    tertiary = color;
                } else {
                    secondary = color;
                }
            }
            wallpaper.primaryColors = new WallpaperColors(primary, secondary, tertiary, getAttributeInt(parser, "colorHints", 0));
        }
        wallpaper.name = parser.getAttributeValue(null, com.android.server.pm.Settings.ATTR_NAME);
        wallpaper.allowBackup = "true".equals(parser.getAttributeValue(null, BatteryService.HealthServiceWrapper.INSTANCE_HEALTHD));
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
            return;
        }
        throw new RuntimeException("settingsRestored() can only be called from the system process");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:51:0x0131, code lost:
        if (0 != 0) goto L_0x0133;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x0133, code lost:
        android.os.FileUtils.sync(null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0136, code lost:
        libcore.io.IoUtils.closeQuietly((java.lang.AutoCloseable) null);
        libcore.io.IoUtils.closeQuietly((java.lang.AutoCloseable) null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x015b, code lost:
        if (0 != 0) goto L_0x0133;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0181, code lost:
        if (0 != 0) goto L_0x0133;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:?, code lost:
        return false;
     */
    private boolean restoreNamedResourceLocked(WallpaperData wallpaper) {
        String pkg;
        String ident;
        String type;
        if (wallpaper.name.length() <= 4 || !"res:".equals(wallpaper.name.substring(0, 4))) {
            return false;
        }
        String resName = wallpaper.name.substring(4);
        int colon = resName.indexOf(58);
        if (colon > 0) {
            pkg = resName.substring(0, colon);
        } else {
            pkg = null;
        }
        int slash = resName.lastIndexOf(47);
        if (slash > 0) {
            ident = resName.substring(slash + 1);
        } else {
            ident = null;
        }
        if (colon <= 0 || slash <= 0 || slash - colon <= 1) {
            type = null;
        } else {
            type = resName.substring(colon + 1, slash);
        }
        if (pkg == null || ident == null || type == null) {
            return false;
        }
        try {
            Resources r = this.mContext.createPackageContext(pkg, 4).getResources();
            int resId = r.getIdentifier(resName, null, null);
            if (resId == 0) {
                Slog.e(TAG, "couldn't resolve identifier pkg=" + pkg + " type=" + type + " ident=" + ident);
                IoUtils.closeQuietly((AutoCloseable) null);
                if (0 != 0) {
                    FileUtils.sync(null);
                }
                if (0 != 0) {
                    FileUtils.sync(null);
                }
                IoUtils.closeQuietly((AutoCloseable) null);
                IoUtils.closeQuietly((AutoCloseable) null);
                return false;
            }
            InputStream res = r.openRawResource(resId);
            if (wallpaper.wallpaperFile.exists()) {
                wallpaper.wallpaperFile.delete();
                wallpaper.cropFile.delete();
            }
            FileOutputStream fos = new FileOutputStream(wallpaper.wallpaperFile);
            FileOutputStream cos = new FileOutputStream(wallpaper.cropFile);
            byte[] buffer = new byte[32768];
            while (true) {
                int amt = res.read(buffer);
                if (amt > 0) {
                    fos.write(buffer, 0, amt);
                    cos.write(buffer, 0, amt);
                } else {
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
            Slog.e(TAG, "Package name " + pkg + " not found");
            IoUtils.closeQuietly((AutoCloseable) null);
            if (0 != 0) {
                FileUtils.sync(null);
            }
        } catch (Resources.NotFoundException e2) {
            Slog.e(TAG, "Resource not found: -1");
            IoUtils.closeQuietly((AutoCloseable) null);
            if (0 != 0) {
                FileUtils.sync(null);
            }
        } catch (IOException e3) {
            Slog.e(TAG, "IOException while restoring wallpaper ", e3);
            IoUtils.closeQuietly((AutoCloseable) null);
            if (0 != 0) {
                FileUtils.sync(null);
            }
        } catch (Throwable th) {
            IoUtils.closeQuietly((AutoCloseable) null);
            if (0 != 0) {
                FileUtils.sync(null);
            }
            if (0 != 0) {
                FileUtils.sync(null);
            }
            IoUtils.closeQuietly((AutoCloseable) null);
            IoUtils.closeQuietly((AutoCloseable) null);
            throw th;
        }
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
                    pw.println(" Display state:");
                    forEachDisplayData(new Consumer(pw) {
                        /* class com.android.server.wallpaper.$$Lambda$WallpaperManagerService$VlOcXJ2BasDkYqNidSTRvwHBpM */
                        private final /* synthetic */ PrintWriter f$0;

                        {
                            this.f$0 = r1;
                        }

                        @Override // java.util.function.Consumer
                        public final void accept(Object obj) {
                            WallpaperManagerService.lambda$dump$6(this.f$0, (WallpaperManagerService.DisplayData) obj);
                        }
                    });
                    pw.print("  mCropHint=");
                    pw.println(wallpaper.cropHint);
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
                        conn.forEachDisplayConnector(new Consumer(pw) {
                            /* class com.android.server.wallpaper.$$Lambda$WallpaperManagerService$fLM_YLhVBfWS7QM0taqHXvJ4Uc */
                            private final /* synthetic */ PrintWriter f$0;

                            {
                                this.f$0 = r1;
                            }

                            @Override // java.util.function.Consumer
                            public final void accept(Object obj) {
                                WallpaperManagerService.lambda$dump$7(this.f$0, (WallpaperManagerService.WallpaperConnection.DisplayConnector) obj);
                            }
                        });
                        pw.print("    mService=");
                        pw.println(conn.mService);
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
                    pw.print("  mCropHint=");
                    pw.println(wallpaper2.cropHint);
                    pw.print("  mName=");
                    pw.println(wallpaper2.name);
                    pw.print("  mAllowBackup=");
                    pw.println(wallpaper2.allowBackup);
                }
                pw.println("Fallback wallpaper state:");
                pw.print(" User ");
                pw.print(this.mFallbackWallpaper.userId);
                pw.print(": id=");
                pw.println(this.mFallbackWallpaper.wallpaperId);
                pw.print("  mCropHint=");
                pw.println(this.mFallbackWallpaper.cropHint);
                pw.print("  mName=");
                pw.println(this.mFallbackWallpaper.name);
                pw.print("  mAllowBackup=");
                pw.println(this.mFallbackWallpaper.allowBackup);
                if (this.mFallbackWallpaper.connection != null) {
                    WallpaperConnection conn2 = this.mFallbackWallpaper.connection;
                    pw.print("  Fallback Wallpaper connection ");
                    pw.print(conn2);
                    pw.println(":");
                    if (conn2.mInfo != null) {
                        pw.print("    mInfo.component=");
                        pw.println(conn2.mInfo.getComponent());
                    }
                    conn2.forEachDisplayConnector(new Consumer(pw) {
                        /* class com.android.server.wallpaper.$$Lambda$WallpaperManagerService$VUhQWq8Flr0dsQqeVHhHT8jU7qY */
                        private final /* synthetic */ PrintWriter f$0;

                        {
                            this.f$0 = r1;
                        }

                        @Override // java.util.function.Consumer
                        public final void accept(Object obj) {
                            WallpaperManagerService.lambda$dump$8(this.f$0, (WallpaperManagerService.WallpaperConnection.DisplayConnector) obj);
                        }
                    });
                    pw.print("    mService=");
                    pw.println(conn2.mService);
                    pw.print("    mLastDiedTime=");
                    pw.println(this.mFallbackWallpaper.lastDiedTime - SystemClock.uptimeMillis());
                }
            }
        }
    }

    static /* synthetic */ void lambda$dump$6(PrintWriter pw, DisplayData wpSize) {
        pw.print("  displayId=");
        pw.println(wpSize.mDisplayId);
        pw.print("  mWidth=");
        pw.print(wpSize.mWidth);
        pw.print("  mHeight=");
        pw.println(wpSize.mHeight);
        pw.print("  mPadding=");
        pw.println(wpSize.mPadding);
    }

    static /* synthetic */ void lambda$dump$7(PrintWriter pw, WallpaperConnection.DisplayConnector connector) {
        pw.print("     mDisplayId=");
        pw.println(connector.mDisplayId);
        pw.print("     mToken=");
        pw.println(connector.mToken);
        pw.print("     mEngine=");
        pw.println(connector.mEngine);
    }

    static /* synthetic */ void lambda$dump$8(PrintWriter pw, WallpaperConnection.DisplayConnector connector) {
        pw.print("     mDisplayId=");
        pw.println(connector.mDisplayId);
        pw.print("     mToken=");
        pw.println(connector.mToken);
        pw.print("     mEngine=");
        pw.println(connector.mEngine);
    }

    public ParcelFileDescriptor getBlurWallpaper(IWallpaperManagerCallback cb) {
        return this.mIHwWMEx.getBlurWallpaper(cb);
    }

    @Override // com.android.server.wallpaper.IHwWallpaperManagerInner
    public SparseArray<WallpaperData> getWallpaperMap() {
        return this.mWallpaperMap;
    }

    public Context getContext() {
        return this.mContext;
    }

    @Override // com.android.server.wallpaper.IHwWallpaperManagerInner
    public Object getLock() {
        return this.mLock;
    }

    @Override // com.android.server.wallpaper.IHwWallpaperManagerInner
    public int getCurrentUserId() {
        return this.mCurrentUserId;
    }

    /* access modifiers changed from: protected */
    public void updateWallpaperOffsets(WallpaperData wallpaper) {
        this.mIHwWMEx.updateWallpaperOffsets(wallpaper);
    }

    public int[] getCurrOffsets() {
        return this.mIHwWMEx.getCurrOffsets();
    }

    public void setCurrOffsets(int[] offsets) {
        this.mIHwWMEx.setCurrOffsets(offsets);
    }

    public void setNextOffsets(int[] offsets) {
        this.mIHwWMEx.setNextOffsets(offsets);
    }

    public int getWallpaperUserId() {
        return this.mCurrentUserId;
    }

    public Bitmap scaleWallpaperBitmapToScreenSize(Bitmap bitmap) {
        return this.mIHwWMEx.scaleWallpaperBitmapToScreenSize(bitmap);
    }

    @Override // com.android.server.wallpaper.IHwWallpaperManagerInner
    public void restartLiveWallpaperService() {
        WallpaperData wallpaper = getWallpaperMap().get(getWallpaperUserId());
        if (wallpaper != null && wallpaper.wallpaperComponent != null) {
            Slog.d(TAG, "restartLiveWallpaperService ");
            bindWallpaperComponentLocked(wallpaper.wallpaperComponent, true, false, wallpaper, null);
        }
    }

    public boolean isMdmDisableChangeWallpaper() {
        if (!HwDeviceManager.disallowOp(35)) {
            return false;
        }
        Slog.d(TAG, "MDM policy forbidden setWallpaper");
        mWHandler.postDelayed(new Runnable() {
            /* class com.android.server.wallpaper.WallpaperManagerService.AnonymousClass6 */

            @Override // java.lang.Runnable
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
        this.mIHwWMEx.reportWallpaper(componentName);
    }

    public ParcelFileDescriptor screenshotLiveWallpaper(float scale, int config) {
        ParcelFileDescriptor fd;
        Trace.traceBegin(8192, "screenshotLiveWallpaper");
        Bitmap.Config bitmapConfig = null;
        Bitmap.Config[] configs = Bitmap.Config.values();
        if (config >= 0 && configs != null && config < configs.length) {
            bitmapConfig = configs[config];
        }
        Bitmap bitmap = null;
        try {
            Bitmap bitmap2 = this.mIWindowManager.screenshotWallpaper();
            if (bitmap2 == null) {
                Slog.e(TAG, "screenshotWallpaper is null");
                if (bitmap2 != null && !bitmap2.isRecycled()) {
                    bitmap2.recycle();
                }
                Trace.traceEnd(8192);
                return null;
            }
            synchronized (SCREEN_SHOT_LIVE_WALLPAPER_LOCK) {
                fd = this.mIHwWMEx.scaleBitmaptoFileDescriptor(bitmap2, scale, bitmapConfig);
            }
            if (!bitmap2.isRecycled()) {
                bitmap2.recycle();
            }
            Trace.traceEnd(8192);
            return fd;
        } catch (RemoteException e) {
            Slog.e(TAG, "screenshotWallpaper failed: " + e.getMessage());
            if (0 != 0 && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
            Trace.traceEnd(8192);
            return null;
        } catch (Throwable th) {
            if (0 != 0 && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
            Trace.traceEnd(8192);
            throw th;
        }
    }
}
