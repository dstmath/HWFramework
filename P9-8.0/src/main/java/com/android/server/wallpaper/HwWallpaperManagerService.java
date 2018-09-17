package com.android.server.wallpaper;

import android.app.IWallpaperManagerCallback;
import android.app.WallpaperInfo;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hwtheme.HwThemeManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFileDescriptor.AutoCloseOutputStream;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SELinux;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.System;
import android.rms.HwSysResManager;
import android.rms.iaware.AwareConstant.ResourceType;
import android.rms.iaware.CollectData;
import android.util.DisplayMetrics;
import android.util.Slog;
import android.view.Display;
import android.view.IWindowManager.Stub;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import com.android.server.BlurUtils;
import com.android.server.SystemService;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.wallpaper.WallpaperManagerService.WallpaperData;
import huawei.android.hwpicaveragenoises.HwPicAverageNoises;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class HwWallpaperManagerService extends WallpaperManagerService {
    public static final boolean DEBUG = false;
    private static final String PERMISSION = "com.huawei.wallpaperservcie.permission.SET_WALLPAPER_OFFSET";
    private static final String ROG_KILL_APP_END_ACTION = "com.huawei.systemmamanger.action.KILL_ROGAPP_END";
    static final String TAG = "HwWallpaperService";
    protected int disHeight;
    protected int disWidth;

    public static class Lifecycle extends SystemService {
        private HwWallpaperManagerService mService;

        public Lifecycle(Context context) {
            super(context);
        }

        public void onStart() {
            this.mService = new HwWallpaperManagerService(getContext());
            publishBinderService("wallpaper", this.mService);
        }

        public void onBootPhase(int phase) {
            if (phase == 550) {
                this.mService.systemRunning();
            } else if (phase == 600) {
                this.mService.switchUser(0, null);
            }
        }

        public void onUnlockUser(int userHandle) {
            this.mService.onUnlockUser(userHandle);
        }
    }

    public HwWallpaperManagerService(Context context) {
        super(context);
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) getContext().getSystemService("window")).getDefaultDisplay().getMetrics(dm);
        this.disWidth = dm.widthPixels;
        this.disHeight = dm.heightPixels;
    }

    public void systemRunning() {
        super.systemReady();
        WallpaperData wallpaper = (WallpaperData) getWallpaperMap().get(0);
        createBlurWallpaper(false);
        synchronized (getLock()) {
            notifyBlurCallbacksLocked(wallpaper);
        }
        IntentFilter userFilter = new IntentFilter();
        userFilter.addAction(ROG_KILL_APP_END_ACTION);
        getContext().registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (HwWallpaperManagerService.ROG_KILL_APP_END_ACTION.equals(intent.getAction())) {
                    HwWallpaperManagerService.this.restartLiveWallpaperService();
                }
            }
        }, userFilter);
    }

    public void handleWallpaperObserverEvent(final WallpaperData wallpaper) {
        new Thread("BlurWallpaperThread") {
            public void run() {
                Slog.d(HwWallpaperManagerService.TAG, "onEvent createBlurBitmap Blur wallpaper Begin");
                long timeBegin = System.currentTimeMillis();
                HwWallpaperManagerService.this.createBlurWallpaper(true);
                Slog.d(HwWallpaperManagerService.TAG, "onEvent createBlurBitmap Blur takenTime = " + (System.currentTimeMillis() - timeBegin));
                synchronized (HwWallpaperManagerService.this.getLock()) {
                    HwWallpaperManagerService.this.notifyBlurCallbacksLocked(wallpaper);
                }
            }
        }.start();
    }

    public void createBlurWallpaper(boolean force) {
        synchronized (getLock()) {
            if (!force) {
                if (new File(Environment.getUserSystemDirectory(getWallpaperUserId()), "blurwallpaper").exists()) {
                    return;
                }
            }
            String bitmapStyle = "";
            try {
                Bitmap wallpaper;
                WallpaperInfo info = getWallpaperInfo(getWallpaperUserId());
                if (info == null) {
                    wallpaper = getCurrentWallpaperLocked(getContext());
                    bitmapStyle = "static";
                } else {
                    Drawable dr = HwFrameworkFactory.getHwWallpaperInfoStub(info).loadThumbnailWithoutTheme(getContext().getPackageManager());
                    if (dr == null) {
                        wallpaper = null;
                    } else if (dr instanceof BitmapDrawable) {
                        wallpaper = ((BitmapDrawable) dr).getBitmap();
                    } else {
                        try {
                            wallpaper = Bitmap.createBitmap(dr.getIntrinsicWidth(), dr.getIntrinsicHeight(), dr.getOpacity() != -1 ? Config.ARGB_8888 : Config.RGB_565);
                            Canvas canvas = new Canvas(wallpaper);
                            dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
                            dr.draw(canvas);
                        } catch (RuntimeException e) {
                            Slog.e(TAG, "create blurwallpaper draw has some errors");
                            wallpaper = null;
                        }
                    }
                    bitmapStyle = "live";
                }
                if (wallpaper == null || wallpaper.isRecycled()) {
                    Slog.d(TAG, "wallpaper is null or has been recycled");
                    wallpaper = getDefaultWallpaperLocked(getContext());
                }
                if (wallpaper != null) {
                    Bitmap bitmap;
                    int h = this.disHeight;
                    int w = this.disWidth;
                    if (!ViewConfiguration.get(getContext()).hasPermanentMenuKey()) {
                        h += getContext().getResources().getDimensionPixelSize(17105141);
                    }
                    if (wallpaper.getHeight() == h && wallpaper.getWidth() == w * 2) {
                        int[] inPixels = new int[(this.disWidth * h)];
                        bitmap = Bitmap.createBitmap(this.disWidth, h, Config.ARGB_8888);
                        wallpaper.getPixels(inPixels, 0, this.disWidth, 0, 0, this.disWidth, h);
                        bitmap.setPixels(inPixels, 0, this.disWidth, 0, 0, this.disWidth, h);
                        if (!(bitmap == wallpaper || (wallpaper.isRecycled() ^ 1) == 0)) {
                            wallpaper.recycle();
                            wallpaper = bitmap;
                            if (info != null) {
                                removeThumbnailCache(info);
                            }
                        }
                    }
                    bitmap = wallpaper;
                    if (!(wallpaper.getHeight() == h / 4 && wallpaper.getWidth() == w / 4)) {
                        wallpaper = Bitmap.createScaledBitmap(bitmap, this.disWidth / 4, h / 4, true);
                        if (!(wallpaper == null || wallpaper == bitmap || (bitmap.isRecycled() ^ 1) == 0)) {
                            bitmap.recycle();
                            if (info != null) {
                                removeThumbnailCache(info);
                            }
                        }
                    }
                }
                if (wallpaper != null) {
                    Bitmap bmp = BlurUtils.stackBlur(wallpaper, 80);
                    if (bmp != null) {
                        wallpaper.recycle();
                        if (HwPicAverageNoises.isAverageNoiseSupported()) {
                            saveBlurWallpaperBitmapLocked(bmp);
                        } else {
                            Bitmap blurImg = BlurUtils.addBlackBoard(bmp, 1275068416);
                            if (blurImg != null) {
                                bmp.recycle();
                                saveBlurWallpaperBitmapLocked(blurImg);
                            }
                        }
                    }
                }
            } catch (Throwable e2) {
                Slog.w(TAG, "No memory load current wallpaper", e2);
            }
        }
        return;
    }

    /* JADX WARNING: Missing block: B:8:0x001d, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ParcelFileDescriptor getBlurWallpaper(IWallpaperManagerCallback cb) {
        synchronized (getLock()) {
            int wallpaperUserId = getWallpaperUserId();
            WallpaperData wallpaper = (WallpaperData) getWallpaperMap().get(wallpaperUserId);
            if (wallpaper == null || wallpaper.getCallbacks() == null) {
            } else {
                if (cb != null) {
                    if (!wallpaper.getCallbacks().isContainIBinder(cb)) {
                        wallpaper.getCallbacks().register(cb);
                    }
                }
                try {
                    File f = new File(Environment.getUserSystemDirectory(wallpaperUserId), "blurwallpaper");
                    if (f.exists()) {
                        ParcelFileDescriptor open = ParcelFileDescriptor.open(f, 268435456);
                        return open;
                    }
                    return null;
                } catch (FileNotFoundException e) {
                    Slog.w(TAG, "Error getting wallpaper", e);
                    return null;
                }
            }
        }
    }

    public int getWallpaperUserId() {
        int callingUid = Binder.getCallingUid();
        if (callingUid == 1000) {
            return getCurrentUserId();
        }
        return UserHandle.getUserId(callingUid);
    }

    private Bitmap getCurrentWallpaperLocked(Context context) {
        ParcelFileDescriptor fd = getWallpaper(new Bundle());
        if (fd != null) {
            try {
                Bitmap bm = scaleWallpaperBitmapToScreenSize(BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null, new Options()));
                try {
                    fd.close();
                } catch (IOException e) {
                }
                return bm;
            } catch (OutOfMemoryError e2) {
                Slog.w(TAG, "Can't decode file", e2);
                try {
                    fd.close();
                } catch (IOException e3) {
                }
            } catch (Throwable th) {
                try {
                    fd.close();
                } catch (IOException e4) {
                }
                throw th;
            }
        }
        return null;
    }

    private Bitmap getDefaultWallpaperLocked(Context context) {
        InputStream is = HwThemeManager.getDefaultWallpaperIS(context, getCurrentUserId());
        if (is != null) {
            try {
                Bitmap bm = scaleWallpaperBitmapToScreenSize(BitmapFactory.decodeStream(is, null, new Options()));
                try {
                    is.close();
                } catch (IOException e) {
                }
                return bm;
            } catch (OutOfMemoryError e2) {
                Slog.w(TAG, "Can't decode stream", e2);
                try {
                    is.close();
                } catch (IOException e3) {
                }
            } catch (Throwable th) {
                try {
                    is.close();
                } catch (IOException e4) {
                }
                throw th;
            }
        }
        return null;
    }

    private void notifyBlurCallbacksLocked(WallpaperData wallpaper) {
        RemoteCallbackList<IWallpaperManagerCallback> callbacks = wallpaper.getCallbacks();
        synchronized (callbacks) {
            int n = callbacks.beginBroadcast();
            for (int i = 0; i < n; i++) {
                try {
                    ((IWallpaperManagerCallback) callbacks.getBroadcastItem(i)).onBlurWallpaperChanged();
                } catch (RemoteException e) {
                }
            }
            callbacks.finishBroadcast();
        }
    }

    public ParcelFileDescriptor getWallpaper(Bundle outParams) {
        synchronized (getLock()) {
            int wallpaperUserId = getWallpaperUserId();
            WallpaperData wallpaper = (WallpaperData) getWallpaperMap().get(wallpaperUserId);
            if (outParams != null) {
                try {
                    outParams.putInt("width", wallpaper.getWidth());
                    outParams.putInt("height", wallpaper.getHeight());
                } catch (FileNotFoundException e) {
                    Slog.w(TAG, "Error getting wallpaper", e);
                    return null;
                }
            }
            File f = new File(Environment.getUserSystemDirectory(wallpaperUserId), "wallpaper");
            if (f.exists()) {
                ParcelFileDescriptor open = ParcelFileDescriptor.open(f, 268435456);
                return open;
            }
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x0092 A:{SYNTHETIC, Splitter: B:29:0x0092} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0097 A:{SYNTHETIC, Splitter: B:32:0x0097} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void saveBlurWallpaperBitmapLocked(Bitmap blurImg) {
        Throwable th;
        Slog.d(TAG, "saveBlurWallpaperBitmapLocked begin");
        long timeBegin = System.currentTimeMillis();
        try {
            File dir = Environment.getUserSystemDirectory(getWallpaperUserId());
            if (!dir.exists() && dir.mkdir()) {
                FileUtils.setPermissions(dir.getPath(), 505, -1, -1);
            }
            File file = new File(dir, "blurwallpaper");
            ParcelFileDescriptor fd = ParcelFileDescriptor.open(file, 939524096);
            if (SELinux.restorecon(file)) {
                FileOutputStream fos = null;
                try {
                    FileOutputStream fos2 = new AutoCloseOutputStream(fd);
                    try {
                        blurImg.compress(CompressFormat.PNG, 90, fos2);
                        if (fos2 != null) {
                            try {
                                fos2.close();
                            } catch (IOException e) {
                            }
                        }
                        if (fd != null) {
                            try {
                                fd.close();
                            } catch (IOException e2) {
                            }
                        }
                        Slog.d(TAG, "saveBlurWallpaperBitmapLocked takenTime = " + (System.currentTimeMillis() - timeBegin));
                    } catch (Throwable th2) {
                        th = th2;
                        fos = fos2;
                        if (fos != null) {
                        }
                        if (fd != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e3) {
                        }
                    }
                    if (fd != null) {
                        try {
                            fd.close();
                        } catch (IOException e4) {
                        }
                    }
                    throw th;
                }
            }
            if (fd != null) {
                try {
                    fd.close();
                } catch (IOException e5) {
                }
            }
        } catch (FileNotFoundException e6) {
            Slog.w(TAG, "Error setting wallpaper", e6);
        }
    }

    /* JADX WARNING: Missing block: B:12:0x002f, code:
            return r19;
     */
    /* JADX WARNING: Missing block: B:37:0x00ea, code:
            return r7;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Bitmap generateBitmap(Context context, Bitmap bm, int width, int height, WallpaperInfo info) {
        synchronized (getLock()) {
            if (context == null || bm == null) {
                return null;
            }
            WindowManager wm = (WindowManager) context.getSystemService("window");
            DisplayMetrics metrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(metrics);
            bm.setDensity(metrics.noncompatDensityDpi);
            if (width > 0 && height > 0) {
                if (!(bm.getWidth() == width && bm.getHeight() == height)) {
                    try {
                        Bitmap newBimap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
                        if (newBimap == null) {
                            Slog.w(TAG, "Can't generate bitmap, newBimap = null");
                            return bm;
                        }
                        newBimap.setDensity(metrics.noncompatDensityDpi);
                        Canvas c = new Canvas(newBimap);
                        Rect targetRect = new Rect();
                        targetRect.right = bm.getWidth();
                        targetRect.bottom = bm.getHeight();
                        int deltaw = width - targetRect.right;
                        int deltah = height - targetRect.bottom;
                        if (deltaw > 0 || deltah > 0) {
                            float tempWidth = ((float) width) / ((float) targetRect.right);
                            float tempHeight = ((float) height) / ((float) targetRect.bottom);
                            float scale = tempWidth > tempHeight ? tempWidth : tempHeight;
                            targetRect.right = (int) (((float) targetRect.right) * scale);
                            targetRect.bottom = (int) (((float) targetRect.bottom) * scale);
                            deltaw = width - targetRect.right;
                            deltah = height - targetRect.bottom;
                        }
                        targetRect.offset(deltaw / 2, deltah / 2);
                        Paint paint = new Paint();
                        paint.setFilterBitmap(true);
                        paint.setXfermode(new PorterDuffXfermode(Mode.SRC));
                        c.drawBitmap(bm, null, targetRect, paint);
                        bm.recycle();
                        if (info != null) {
                            removeThumbnailCache(info);
                        }
                    } catch (OutOfMemoryError e) {
                        Slog.w(TAG, "Can't generate bitmap:", e);
                        return bm;
                    }
                }
            }
        }
    }

    private void removeThumbnailCache(WallpaperInfo info) {
        if (info != null) {
            info.removeThumbnailCache(getContext().getPackageManager());
        }
    }

    protected boolean getHwWallpaperWidth(WallpaperData wallpaper, boolean success) {
        if (success) {
            return false;
        }
        Display d = ((WindowManager) getContext().getSystemService("window")).getDefaultDisplay();
        Point pSize = new Point();
        d.getSize(pSize);
        wallpaper.setWidth(SystemProperties.getInt("ro.config.hwwallpaperwidth", pSize.x * 2));
        Slog.i(TAG, "loadSettings, mWidth:" + wallpaper.getWidth() + ", mHeight:" + wallpaper.getHeight() + ", display size:" + pSize);
        return true;
    }

    private void saveWallpaperOffsets(int[] offsets) {
        if (offsets != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < offsets.length; i++) {
                sb.append(offsets[i]);
                if (i < offsets.length - 1) {
                    sb.append(",");
                }
            }
            int userId = getWallpaperUserId();
            System.putStringForUser(getContext().getContentResolver(), "curr_wallpaper_offsets", sb.toString(), userId);
            Slog.i(TAG, "saveWallpaperOffsets(" + userId + "):" + sb);
        }
    }

    protected void updateWallpaperOffsets(WallpaperData wallpaper) {
        Bitmap bm = getCurrentWallpaperLocked(getContext());
        if (bm != null) {
            DisplayMetrics metrics = new DisplayMetrics();
            Display display = ((WindowManager) getContext().getSystemService("window")).getDefaultDisplay();
            display.getMetrics(metrics);
            bm.setDensity(metrics.noncompatDensityDpi);
            Point size = new Point();
            display.getRealSize(size);
            int min = size.x < size.y ? size.x : size.y;
            int max = size.x > size.y ? size.x : size.y;
            int width = bm.getWidth();
            int height = bm.getHeight();
            if ((height == max || width == max) && (width == min * 2 || width == max)) {
                if (wallpaper.nextOffsets[0] == -1) {
                    wallpaper.currOffsets[0] = -1;
                } else if (wallpaper.nextOffsets[0] < -1) {
                    wallpaper.currOffsets[0] = 0;
                } else if (wallpaper.nextOffsets[0] <= width - min) {
                    wallpaper.currOffsets[0] = wallpaper.nextOffsets[0];
                } else if (wallpaper.nextOffsets[0] > width - min) {
                    wallpaper.currOffsets[0] = width - min;
                }
                wallpaper.currOffsets[1] = 0;
                wallpaper.currOffsets[2] = 0;
                if (wallpaper.nextOffsets[3] == -1) {
                    wallpaper.currOffsets[3] = -1;
                } else if (wallpaper.nextOffsets[3] < -1) {
                    wallpaper.currOffsets[3] = 0;
                } else if (wallpaper.nextOffsets[3] <= height - min) {
                    wallpaper.currOffsets[3] = wallpaper.nextOffsets[3];
                } else if (wallpaper.nextOffsets[3] > height - min) {
                    wallpaper.currOffsets[3] = height - min;
                }
            } else {
                Slog.w(TAG, "Irregular bitmap: width=" + width + ", height=" + height);
                for (int i = 0; i < wallpaper.currOffsets.length; i++) {
                    wallpaper.currOffsets[i] = -1;
                }
            }
            saveWallpaperOffsets(wallpaper.currOffsets);
            if (!bm.isRecycled()) {
                bm.recycle();
            }
        }
    }

    public int[] getCurrOffsets() throws RemoteException {
        int[] offsets;
        synchronized (getLock()) {
            offsets = new int[]{-1, -1, -1, -1};
            int userId = getWallpaperUserId();
            WallpaperData wallpaper = (WallpaperData) getWallpaperMap().get(userId);
            String wallpaperOffsets = System.getStringForUser(getContext().getContentResolver(), "curr_wallpaper_offsets", userId);
            if (wallpaperOffsets != null) {
                String[] str = wallpaperOffsets.split(",");
                if (str.length == wallpaper.currOffsets.length) {
                    for (int i = 0; i < wallpaper.currOffsets.length; i++) {
                        wallpaper.currOffsets[i] = Integer.parseInt(str[i]);
                    }
                }
            }
            System.arraycopy(wallpaper.currOffsets, 0, offsets, 0, wallpaper.currOffsets.length);
            Slog.i(TAG, "getCurrOffsets(" + userId + "):" + offsets[0] + "," + offsets[1] + "," + offsets[2] + "," + offsets[3]);
        }
        return offsets;
    }

    public void setCurrOffsets(int[] offsets) throws RemoteException {
        getContext().enforceCallingOrSelfPermission(PERMISSION, null);
        synchronized (getLock()) {
            WallpaperData wallpaper = (WallpaperData) getWallpaperMap().get(getWallpaperUserId());
            if (!(offsets == null || wallpaper == null || offsets.length != wallpaper.currOffsets.length)) {
                System.arraycopy(offsets, 0, wallpaper.currOffsets, 0, wallpaper.currOffsets.length);
                Slog.i(TAG, "setCurrOffsets:" + offsets[0] + "," + offsets[1] + "," + offsets[2] + "," + offsets[3]);
            }
        }
    }

    public void setNextOffsets(int[] offsets) throws RemoteException {
        synchronized (getLock()) {
            WallpaperData wallpaper = (WallpaperData) getWallpaperMap().get(UserHandle.getCallingUserId());
            if (!(offsets == null || wallpaper == null || offsets.length != wallpaper.nextOffsets.length)) {
                System.arraycopy(offsets, 0, wallpaper.nextOffsets, 0, wallpaper.nextOffsets.length);
                Slog.i(TAG, "setNextOffsets:" + offsets[0] + "," + offsets[1] + "," + offsets[2] + "," + offsets[3]);
            }
        }
    }

    public Bitmap scaleWallpaperBitmapToScreenSize(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        Bitmap bmp;
        Matrix m = new Matrix();
        Point size = new Point();
        int longSideLength = 0;
        int shortSideLength = 0;
        int bitmap_height = bitmap.getHeight();
        int bitmap_width = bitmap.getWidth();
        int bitmapLongSideLength = bitmap_height > bitmap_width ? bitmap_height : bitmap_width;
        int bitmapShortSideLength = bitmap_height > bitmap_width ? bitmap_width : bitmap_height;
        try {
            Stub.asInterface(ServiceManager.checkService("window")).getBaseDisplaySize(0, size);
            longSideLength = size.x > size.y ? size.x : size.y;
            shortSideLength = size.x > size.y ? size.y : size.x;
        } catch (RemoteException e) {
            Slog.e(TAG, "IWindowManager null");
        }
        if (longSideLength == 0 || shortSideLength == 0 || bitmapLongSideLength == longSideLength || (bitmapLongSideLength == shortSideLength * 2 && bitmapShortSideLength == longSideLength)) {
            bmp = bitmap;
        } else {
            if ((((float) bitmapLongSideLength) / ((float) shortSideLength)) / (((float) bitmapShortSideLength) / ((float) longSideLength)) == 2.0f) {
                longSideLength = shortSideLength * 2;
            }
            float scale = ((float) longSideLength) / ((float) bitmapLongSideLength);
            m.setScale(scale, scale);
            bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap_width, bitmap_height, m, true);
        }
        return bmp;
    }

    private void restartLiveWallpaperService() {
        WallpaperData wallpaper = (WallpaperData) getWallpaperMap().get(getWallpaperUserId());
        if (wallpaper != null && wallpaper.wallpaperComponent != null) {
            Slog.d(TAG, "restartLiveWallpaperService ");
            bindWallpaperComponentLocked(wallpaper.wallpaperComponent, true, false, wallpaper, null);
        }
    }

    public void reportWallpaper(ComponentName componentName) {
        if (componentName != null) {
            HwSysResManager resManager = HwSysResManager.getInstance();
            if (resManager != null && resManager.isResourceNeeded(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC))) {
                Bundle bundleArgs = new Bundle();
                bundleArgs.putString(MemoryConstant.MEM_PREREAD_ITEM_NAME, componentName.getPackageName());
                bundleArgs.putInt("relationType", 17);
                CollectData data = new CollectData(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC), System.currentTimeMillis(), bundleArgs);
                long id = Binder.clearCallingIdentity();
                resManager.reportData(data);
                Binder.restoreCallingIdentity(id);
            }
        }
    }
}
